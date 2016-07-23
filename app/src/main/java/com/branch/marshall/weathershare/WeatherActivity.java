package com.branch.marshall.weathershare;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.branch.marshall.weathershare.api.ApiClient;
import com.branch.marshall.weathershare.api.ApiResponse;
import com.branch.marshall.weathershare.api.CurrentWeatherResponse;
import com.branch.marshall.weathershare.api.ForecastResponse;
import com.branch.marshall.weathershare.api.view.ForecastWeatherView;
import com.branch.marshall.weathershare.api.view.TodayWeatherView;
import com.branch.marshall.weathershare.util.EventManager;
import com.branch.marshall.weathershare.util.ImageUtils;
import com.branch.marshall.weathershare.util.events.CityForecastEvent;
import com.branch.marshall.weathershare.util.events.CityWeatherEvent;
import com.branch.marshall.weathershare.util.events.ErrorEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Subscribe;

import java.util.Map;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;


/**
 * Created by marshall on 3/16/16.
 */
public class WeatherActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String LOG_TAG = "WeatherShare";

    private static void Log(String msg, Object... args) {
        Log.d(LOG_TAG, String.format(msg, args));
    }

    private static final String MATCH_COORDINATE_REGEX = "[+-]?[0-9]*\\.?[0-9]+";
    private static final String SHARE_CITY_TODAY_NAME = "today_city_name";
    private static final String SHARE_CITY_TODAY_LAT = "lat";
    private static final String SHARE_CITY_TODAY_LON = "lon";
    private static final String SHARE_CITY_MODE = "mode";

    private static final int MODE_CURRENT_WEATHER = 0;
    private static final int MODE_5_DAY_FORECAST = 1;

    private GoogleMap mMap;
    private GoogleApiClient mApiClient;

    private LatLng mMyLocation;

    // MC -- 2016-04-22 -- TODO: Move this stuff into a specialized view.
    private Button mShare;

    private View mLoader;
    private LinearLayout mResult;
    private ToggleButton mCurrentButton;
    private ToggleButton mForecastButton;

    private int mCurrentMode;

    private String mCurrentCity;

    private CurrentWeatherResponse mCurrentWeatherResponse;
    private ForecastResponse mForecastResponse;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather);

        if (mApiClient == null) {
            mApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mShare = (Button) findViewById(R.id.shareButton);
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareCityTemperature();
            }
        });
        mShare.setVisibility(View.GONE);

        mLoader = findViewById(R.id.loader);
        mLoader.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mLoader.setVisibility(View.GONE);

        mResult = (LinearLayout) findViewById(R.id.resultLayout);

        mCurrentButton = (ToggleButton) findViewById(R.id.btnCurrent);
        mCurrentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowCurrentWeather();
            }
        });
        mCurrentButton.setChecked(true);

        mForecastButton = (ToggleButton) findViewById(R.id.btnForecast);
        mForecastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowForecastWeather();
            }
        });

        EventManager.getInstance().registerListener(this);
        ImageUtils.getInstance().init(this);
    }

    public void onStart() {
        mApiClient.connect();

        super.onStart();

        Branch branch = Branch.getInstance();

        branch.initSession(new Branch.BranchUniversalReferralInitListener() {
            @Override
            public void onInitFinished(BranchUniversalObject branchUniversalObject, LinkProperties linkProperties, BranchError error) {
                if (error == null) {
                    if (branchUniversalObject != null) {
                        Map<String, String> data = branchUniversalObject.getMetadata();

                        if (data.containsKey(SHARE_CITY_MODE)) {
                            if (Integer.valueOf(data.get(SHARE_CITY_MODE)) == MODE_5_DAY_FORECAST)
                                setShowForecastWeather();
                            else if (Integer.valueOf(data.get(SHARE_CITY_MODE)) == MODE_CURRENT_WEATHER)
                                setShowCurrentWeather();
                        } else {
                            setShowCurrentWeather();
                        }

                        if (data.containsKey(SHARE_CITY_TODAY_NAME)) {
                            mCurrentCity = data.get(SHARE_CITY_TODAY_NAME);

                            if (MODE_5_DAY_FORECAST == mCurrentMode)
                                getCityForecast(mCurrentCity);
                            else
                                getCityCurrentWeather(mCurrentCity);

                            View view = WeatherActivity.this.getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                        } else if (data.containsKey(SHARE_CITY_TODAY_LAT)) {
                            if (MODE_5_DAY_FORECAST == mCurrentMode)
                                getLocationForecast(Double.valueOf(data.get(SHARE_CITY_TODAY_LAT)), Double.valueOf(data.get(SHARE_CITY_TODAY_LON)));
                            else
                                getLocationCurrentWeather(Double.valueOf(data.get(SHARE_CITY_TODAY_LAT)), Double.valueOf(data.get(SHARE_CITY_TODAY_LON)));

                            View view = WeatherActivity.this.getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                        }
                    }
                } else {
                    Log(error.getMessage());
                }
            }
        }, this.getIntent().getData(), this);
    }

    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
        handleIntent(intent);
    }

    public void onStop() {
        mApiClient.disconnect();

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        MenuItemCompat.expandActionView(menu.findItem(R.id.search));

        return true;
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            mForecastResponse = null;
            mCurrentWeatherResponse = null;

            mCurrentCity = query;

            if (mCurrentMode == MODE_5_DAY_FORECAST)
                getCityForecast(query);
            else
                getCityCurrentWeather(query);
        }
    }

    private void startLoading() {
        mLoader.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoader.setVisibility(View.GONE);
    }

    private void setShowCurrentWeather() {
        mCurrentButton.setChecked(true);
        mForecastButton.setChecked(false);

        if (mCurrentMode == MODE_CURRENT_WEATHER)
            return;

        // Do we already have data for this city?
        if (mCurrentWeatherResponse != null) {
            TodayWeatherView view = new TodayWeatherView(this);

            view.setWeatherData(mCurrentWeatherResponse);

            mResult.removeAllViews();
            mResult.addView(view);
        } else if (mCurrentCity != null) {
            getCityCurrentWeather(mCurrentCity);
        }

        mCurrentMode = MODE_CURRENT_WEATHER;
    }

    private void setShowForecastWeather() {
        mCurrentButton.setChecked(false);
        mForecastButton.setChecked(true);

        if (mCurrentMode == MODE_5_DAY_FORECAST)
            return;

        if (mForecastResponse != null) {
            ForecastWeatherView view = new ForecastWeatherView(this);

            view.setResponseData(mForecastResponse);

            mResult.removeAllViews();
            mResult.addView(view);
        } else if (mCurrentCity != null) {
            getCityForecast(mCurrentCity);
        }

        mCurrentMode = MODE_5_DAY_FORECAST;
    }

    private void getCityCurrentWeather(String city) {
        startLoading();
        mShare.setVisibility(View.GONE);
        ApiClient.getInstance().getCurrentWeatherForCity(city);
    }

    private void getCityForecast(String city) {
        startLoading();
        mShare.setVisibility(View.GONE);
        ApiClient.getInstance().getForecastForCity(city);
    }

    private void getLocationCurrentWeather(double lat, double lon) {
        startLoading();
        mShare.setVisibility(View.GONE);
        ApiClient.getInstance().getCurrentWeatherForLocation(lat, lon);
    }

    private void getLocationForecast(double lat, double lon) {
        startLoading();
        mShare.setVisibility(View.GONE);
        ApiClient.getInstance().getForecastForLocation(lat, lon);
    }

    private void shareCityTemperature() {
        ApiResponse response = (MODE_5_DAY_FORECAST == mCurrentMode) ? (mForecastResponse) : (mCurrentWeatherResponse);

        BranchUniversalObject obj = new BranchUniversalObject()
                .setTitle(response.getCityName())
                .setContentDescription(getResources().getString(R.string.share_body, mCurrentCity))
                .setContentImageUrl(mCurrentWeatherResponse.getWeatherIconUrl())
                .addContentMetadata(SHARE_CITY_TODAY_LAT, "" + response.getLatitude())
                .addContentMetadata(SHARE_CITY_TODAY_LON, "" + response.getLongitude())
                .addContentMetadata(SHARE_CITY_MODE, "" + mCurrentMode);

        LinkProperties linkProperties = new LinkProperties()
                .setChannel("app")
                .setFeature("sharing");

        ShareSheetStyle style = new ShareSheetStyle(this, getResources().getString(R.string.share_title), getResources().getString(R.string.share_body, mCurrentCity));

        startLoading();

        obj.showShareSheet(this, linkProperties, style, new Branch.BranchLinkShareListener() {
            @Override
            public void onShareLinkDialogLaunched() {
                stopLoading();
            }

            @Override
            public void onShareLinkDialogDismissed() {

            }

            @Override
            public void onLinkShareResponse(String sharedLink, String sharedChannel, BranchError error) {

            }

            @Override
            public void onChannelSelected(String channelName) {

            }
        });
    }

    @Subscribe
    public void onCityTemperatureEvent(CityWeatherEvent event) {
        // Did we (somehow) switch the mode
        if (mCurrentMode != MODE_CURRENT_WEATHER)
            return;

        stopLoading();

        // Clear any existing results.
        mResult.removeAllViews();

        mCurrentWeatherResponse = event.getResponse();
        mCurrentCity = mCurrentWeatherResponse.getCityName();

        TodayWeatherView view = new TodayWeatherView(this);

        view.setWeatherData(mCurrentWeatherResponse);

        mResult.addView(view);
        mShare.setVisibility(View.VISIBLE);

        if (mMap != null) {
            mMyLocation = new LatLng(mCurrentWeatherResponse.getLatitude(), mCurrentWeatherResponse.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMyLocation, 12));
        }
    }

    @Subscribe
    public void onCityForecastEvent(CityForecastEvent event) {
        // Did we (somehow) switch the mode
        if (mCurrentMode != MODE_5_DAY_FORECAST)
            return;

        stopLoading();

        // Clear any existing results.
        mResult.removeAllViews();

        mForecastResponse = event.getForecast();
        mCurrentCity = mForecastResponse.getCityName();

        ForecastWeatherView view = new ForecastWeatherView(this);

        view.setResponseData(mForecastResponse);

        mResult.addView(view);
        mShare.setVisibility(View.VISIBLE);

        if (mMap != null) {
            mMyLocation = new LatLng(mForecastResponse.getLatitude(), mForecastResponse.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMyLocation, 12));
        }
    }

    @Subscribe
    public void onError(ErrorEvent event) {
        new AlertDialog.Builder(this)
                .setMessage(event.getThrowableError().getLocalizedMessage())
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();

        mLoader.setVisibility(View.GONE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getMyLocation();

        if (mMyLocation != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMyLocation, 12));
    }

    //////////////////////////////////////////////////////////////
    // GoogleApiClient.ConnectionCallbacks implementation
    //////////////////////////////////////////////////////////////

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Location loc = LocationServices.FusedLocationApi.getLastLocation(mApiClient);

        if (loc != null) {
            mMyLocation = new LatLng(loc.getLatitude(), loc.getLongitude());

            if (mMap != null)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMyLocation, 12));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    //////////////////////////////////////////////////////////////
    // GoogleApiClient.OnConnectionFailedListener implementation
    //////////////////////////////////////////////////////////////

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

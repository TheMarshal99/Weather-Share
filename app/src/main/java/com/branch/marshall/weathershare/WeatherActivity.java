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
import android.widget.Button;
import android.widget.LinearLayout;

import com.branch.marshall.weathershare.api.ApiClient;
import com.branch.marshall.weathershare.api.WeatherResponse;
import com.branch.marshall.weathershare.api.view.TodayWeatherView;
import com.branch.marshall.weathershare.util.EventManager;
import com.branch.marshall.weathershare.util.ImageUtils;
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

    private GoogleMap mMap;
    private GoogleApiClient mApiClient;

    private LatLng mMyLocation;

    // MC -- 2016-04-22 -- TODO: Move this stuff into a specialized view.
    private Button mShare;

    private View mLoader;
    private LinearLayout mResult;

    private WeatherResponse mResponse;

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

        mLoader = findViewById(R.id.loader);
        mLoader.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mLoader.setVisibility(View.GONE);

        mResult = (LinearLayout) findViewById(R.id.resultLayout);
        mResult.setVisibility(View.GONE);

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

                        if (data.containsKey(SHARE_CITY_TODAY_NAME))
                            getCityTemperature(data.get(SHARE_CITY_TODAY_NAME));
                        else if (data.containsKey(SHARE_CITY_TODAY_LAT))
                            getLocationTemperature(Double.valueOf(data.get(SHARE_CITY_TODAY_LAT)), Double.valueOf(data.get(SHARE_CITY_TODAY_LON)));
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
            getCityTemperature(query);
        }
    }

    private void startLoading() {
        mLoader.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        mLoader.setVisibility(View.GONE);
    }

    private void getCityTemperature(String city) {
        startLoading();

        mResult.setVisibility(View.GONE);

        ApiClient.getInstance().getWeatherForCity(city);
    }

    private void getLocationTemperature(double lat, double lon) {
        startLoading();

        mResult.setVisibility(View.GONE);

        ApiClient.getInstance().getWeatherForLocation(lat, lon);
    }

    private void shareCityTemperature() {
        BranchUniversalObject obj = new BranchUniversalObject()
                .addContentMetadata(SHARE_CITY_TODAY_LAT, "" + mResponse.getLatitude())
                .addContentMetadata(SHARE_CITY_TODAY_LON, "" + mResponse.getLongitude());

        LinkProperties linkProperties = new LinkProperties()
                .setChannel("app")
                .setFeature("sharing");

        ShareSheetStyle style = new ShareSheetStyle(this, getResources().getString(R.string.share_title), getResources().getString(R.string.share_body, mResponse.getName()));

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
        stopLoading();

        mResult.setVisibility(View.VISIBLE);

        // Clear any existing results.
        if (mResult.getChildCount() > 1)
            mResult.removeViewAt(0);

        mResponse = event.getResponse();

        TodayWeatherView view = new TodayWeatherView(this);

        view.setWeatherData(mResponse);

        mResult.addView(view, 0);

        if (mMap != null) {
            mMyLocation = new LatLng(mResponse.getLatitude(), mResponse.getLongitude());
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

                    }
                })
                .create()
                .show();

        mLoader.setVisibility(View.GONE);
        mResult.setVisibility(View.GONE);
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

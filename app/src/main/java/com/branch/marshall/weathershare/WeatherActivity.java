package com.branch.marshall.weathershare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.branch.marshall.weathershare.api.ApiClient;
import com.branch.marshall.weathershare.api.WeatherResponse;
import com.branch.marshall.weathershare.util.EventManager;
import com.branch.marshall.weathershare.util.ImageUtils;
import com.branch.marshall.weathershare.util.events.CityWeatherEvent;
import com.branch.marshall.weathershare.util.events.ErrorEvent;
import com.squareup.otto.Subscribe;

import java.util.Map;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.LinkProperties;

/**
 * Created by marshall on 3/16/16.
 */
public class WeatherActivity extends Activity {
    private static final String SHARE_CITY_TODAY_NAME = "today_city_name";

    private EditText mEntry;

    private Button mSearch;
    private Button mShare;

    private ImageView mWeatherImage;
    private TextView mTemp;
    private TextView mCity;
    private TextView mCondition;

    private View mLoader;
    private View mResult;

    private WeatherResponse mResponse;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather);

        mWeatherImage = (ImageView) findViewById(R.id.weatherImage);
        mTemp = (TextView) findViewById(R.id.tempDegrees);
        mCity = (TextView) findViewById(R.id.cityName);
        mCondition = (TextView) findViewById(R.id.weatherType);

        mSearch = (Button) findViewById(R.id.searchButton);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCityTemperature(mEntry.getText().toString().trim());
            }
        });

        mShare = (Button) findViewById(R.id.shareButton);
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareCityTemperature();
            }
        });

        mEntry = (EditText) findViewById(R.id.searchText);
        mEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String t = s.toString();
                mSearch.setEnabled(!TextUtils.isEmpty(t));
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

        mResult = findViewById(R.id.resultLayout);
        mResult.setVisibility(View.GONE);

        EventManager.getInstance().registerListener(this);
        ImageUtils.getInstance().init(this);
    }

    public void onStart() {
        super.onStart();

        Branch branch = Branch.getInstance();

        branch.initSession(new Branch.BranchUniversalReferralInitListener() {
            @Override
            public void onInitFinished(BranchUniversalObject branchUniversalObject, LinkProperties linkProperties, BranchError error) {
                if (error == null) {
                    if (branchUniversalObject != null) {
                        Map<String, String> data = branchUniversalObject.getMetadata();

                        Log.d("WeatherShare", "Got deeplink!");

                        for (String key : data.keySet()) {
                            Log.d("WeatherShare", String.format("%s: %s", key, data.get(key)));
                        }
                    }
                    // params are the deep linked params associated with the link that the user clicked -> was re-directed to this app
                    // params will be empty if no data found
                    // ... insert custom logic here ...
                } else {
                    Log.i("MyApp", error.getMessage());
                }
            }
        }, this.getIntent().getData(), this);
    }

    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
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

    private void shareCityTemperature() {
        BranchUniversalObject obj = new BranchUniversalObject()
                .addContentMetadata(SHARE_CITY_TODAY_NAME, mResponse.getName());

        LinkProperties linkProperties = new LinkProperties()
                .setChannel("app")
                .setFeature("sharing");

        startLoading();

        obj.generateShortUrl(this, linkProperties, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if (error == null) {
                    stopLoading();

                    Log.d("MyApp", String.format("Branch Link: %s", url));
                }
            }
        });
    }

    @Subscribe
    public void onCityTemperatureEvent(CityWeatherEvent event) {
        stopLoading();

        mResult.setVisibility(View.VISIBLE);

        mResponse = event.getResponse();

        mWeatherImage.setImageBitmap(null);
        ImageUtils.getInstance().loadImage(mResponse.getWeatherIconUrl(), mWeatherImage);
        mCity.setText(mResponse.getName());
        mTemp.setText(getResources().getString(R.string.temperature, mResponse.getTemperature()));
        mCondition.setText(capitalizeSentence(mResponse.getConditions()));
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

    private String capitalizeSentence(String in) {
        String[] split = in.split("\\s");
        StringBuilder out = new StringBuilder();

        for (String word : split) {
            out.append(word.substring(0, 1).toUpperCase());
            out.append(word.substring(1));
            out.append(" ");
        }

        return out.toString().trim();
    }
}

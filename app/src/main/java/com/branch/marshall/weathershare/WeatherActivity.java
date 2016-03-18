package com.branch.marshall.weathershare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.branch.marshall.weathershare.api.ApiClient;
import com.branch.marshall.weathershare.api.WeatherResponse;
import com.branch.marshall.weathershare.util.EventManager;
import com.branch.marshall.weathershare.util.events.CityWeatherEvent;
import com.branch.marshall.weathershare.util.events.ErrorEvent;
import com.squareup.otto.Subscribe;

/**
 * Created by marshall on 3/16/16.
 */
public class WeatherActivity extends Activity {
    private EditText mEntry;

    private Button mSearch;

    private TextView mTemp;
    private TextView mCity;
    private TextView mCondition;

    private View mLoader;
    private View mResult;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather);

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
    }

    private void getCityTemperature(String city) {
        mResult.setVisibility(View.GONE);
        mLoader.setVisibility(View.VISIBLE);

        ApiClient.getInstance().getWeatherForCity(city);
    }

    @Subscribe
    public void onCityTemperatureEvent(CityWeatherEvent event) {
        mLoader.setVisibility(View.GONE);
        mResult.setVisibility(View.VISIBLE);

        WeatherResponse response = event.getResponse();

        mCity.setText(response.getName());

        mTemp.setText(getResources().getString(R.string.temperature, response.getTemperature()));
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
}

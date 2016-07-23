package com.branch.marshall.weathershare.api.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.branch.marshall.weathershare.R;
import com.branch.marshall.weathershare.api.ForecastResponse;

/**
 * Created by marshall on 7/22/16.
 */
public class ForecastWeatherView extends LinearLayout {
    private ForecastResponse mResponseData;

    private TextView mCityName;
    private LinearLayout mForecastData;

    public ForecastWeatherView(Context context) {
        super(context);
        init();
    }

    public ForecastWeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ForecastWeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_forecast, this);

        mCityName = (TextView) findViewById(R.id.cityName);
        mForecastData = (LinearLayout) findViewById(R.id.forecastData);


    }

    public void setResponseData(ForecastResponse response) {
        mResponseData = response;

        mCityName.setText(mResponseData.getCityName());

        mForecastData.removeAllViews();

        for (int i = 0; i < 5; i++) {
            ForecastDayView view = new ForecastDayView(getContext());
            view.setData(mResponseData.getForecastAtIndex(i));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            view.setLayoutParams(params);

            mForecastData.addView(view);
        }
    }
}

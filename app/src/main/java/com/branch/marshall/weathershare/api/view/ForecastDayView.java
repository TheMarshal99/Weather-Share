package com.branch.marshall.weathershare.api.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.branch.marshall.weathershare.R;
import com.branch.marshall.weathershare.api.model.ForecastData;
import com.branch.marshall.weathershare.util.ImageUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by marshall on 7/22/16.
 */
public class ForecastDayView extends LinearLayout {
    private ForecastData mForecast;

    private TextView mDay;
    private ImageView mIcon;
    private TextView mCurrentTemp;
    private TextView mHighlow;

    public ForecastDayView(Context context) {
        super(context);
        init();
    }

    public ForecastDayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ForecastDayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_forecast_day, this);

        mDay = (TextView) findViewById(R.id.dayOfWeek);
        mIcon = (ImageView) findViewById(R.id.weatherImage);
        mCurrentTemp = (TextView) findViewById(R.id.tempDegrees);
        mHighlow = (TextView) findViewById(R.id.tempRange);
    }

    public void setData(ForecastData data) {
        mForecast = data;

        Date date = new Date(mForecast.getForecastTime() * 1000);
        SimpleDateFormat fmt = new SimpleDateFormat("EEE");

        mDay.setText(fmt.format(date));

        ImageUtils.getInstance().loadImage(mForecast.getWeatherIconUrl(), mIcon);

        mCurrentTemp.setText(getResources().getString(R.string.temperature, data.getTemperature()));
        mHighlow.setText(getResources().getString(R.string.temperature_hi_low, data.getHigh(), data.getLow()));
    }


}

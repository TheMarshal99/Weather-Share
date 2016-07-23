package com.branch.marshall.weathershare.api.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.branch.marshall.weathershare.R;
import com.branch.marshall.weathershare.api.CurrentWeatherResponse;
import com.branch.marshall.weathershare.util.ImageUtils;

/**
 * View displaying the data for today's weather.
 */
public class TodayWeatherView extends RelativeLayout {
    private ImageView mWeatherImage;
    private TextView mTemp;
    private TextView mCity;
    private TextView mCondition;
    private TextView mRange;

    private CurrentWeatherResponse mResponse;

    public TodayWeatherView(Context context) {
        super(context);
        init();
    }

    public TodayWeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TodayWeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_today, this);

        mWeatherImage = (ImageView) findViewById(R.id.weatherImage);
        mTemp = (TextView) findViewById(R.id.tempDegrees);
        mCity = (TextView) findViewById(R.id.cityName);
        mCondition = (TextView) findViewById(R.id.weatherType);
        mRange = (TextView) findViewById(R.id.tempRange);
    }

    public void setWeatherData(CurrentWeatherResponse response) {
        mResponse = response;

        mWeatherImage.setImageBitmap(null);
        ImageUtils.getInstance().loadImage(mResponse.getWeatherIconUrl(), mWeatherImage);
        mCity.setText(mResponse.getCityName());
        mTemp.setText(getResources().getString(R.string.temperature, mResponse.getTemperature()));
        mRange.setText(getResources().getString(R.string.temperature_range, mResponse.getHigh(), mResponse.getLow()));
        mCondition.setText(capitalizeSentence(mResponse.getConditions()));
    }

    /**
     * Capitalizes each word in a sentence.
     *
     * @param in
     * @return
     */
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

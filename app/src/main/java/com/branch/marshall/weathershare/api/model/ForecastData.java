package com.branch.marshall.weathershare.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by marshall on 7/22/16.
 */
public class ForecastData {
    @SerializedName("dt")
    private Long mForecastTime;

    @SerializedName("temp")
    private DayTemperatureData mTempData;

    @SerializedName("weather")
    private WeatherConditionData[] mConditions;


    public long getForecastTime() {
        return mForecastTime;
    }

    public Double getTemperature() {
        return mTempData.getDayTemp();
    }

    public double getHigh() {
        return mTempData.getHighTemp();
    }

    public double getLow() {
        return mTempData.getLowTemp();
    }

    public String getConditions() {
        return mConditions[0].getDescription();
    }

    public String getWeatherIconUrl() {
        return mConditions[0].getIconUrl();
    }
}

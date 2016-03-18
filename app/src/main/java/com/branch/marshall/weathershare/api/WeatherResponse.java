package com.branch.marshall.weathershare.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by marshall on 3/17/16.
 */
public class WeatherResponse {
    @SerializedName("name")
    private String mName;

    @SerializedName("main")
    private TemperatureData mTempData;

    public String getName() {
        return mName;
    }

    public Double getTemperature() {
        return mTempData.getTemp();
    }

    public double getHigh() {
        return mTempData.getTempMax();
    }

    public double getLow() {
        return mTempData.getTempMin();
    }
}

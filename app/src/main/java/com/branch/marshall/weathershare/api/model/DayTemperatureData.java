package com.branch.marshall.weathershare.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by marshall on 7/22/16.
 */
public class DayTemperatureData {
    @SerializedName("day")
    private Double mDayTemp;

    @SerializedName("min")
    private Double mLowTemp;

    @SerializedName("max")
    private Double mHighTemp;

    public Double getDayTemp() {
        return mDayTemp;
    }

    public Double getLowTemp() {
        return mLowTemp;
    }

    public Double getHighTemp() {
        return mHighTemp;
    }
}

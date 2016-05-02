package com.branch.marshall.weathershare.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by marshall on 4/22/16.
 */
public class LocationData {
    @SerializedName("lat")
    private Double mLatitude;

    @SerializedName("lon")
    private Double mLongitude;

    public double getLatitude() {
        return (mLatitude != null) ? (mLatitude) : (0);
    }

    public double getLongitude() {
        return (mLongitude != null) ? (mLongitude) : (0);
    }
}

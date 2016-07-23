package com.branch.marshall.weathershare.api.model;

import com.google.gson.annotations.SerializedName;

/**
 *
 */
public class CityData {
    @SerializedName("id")
    private Integer mId;

    @SerializedName("name")
    private String mName;

    @SerializedName("coord")
    private LocationData mLocation;

    @SerializedName("country")
    private String mCountryCode;

    public String getName() {
        return mName;
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    public double getLatitude() {
        return mLocation.getLatitude();
    }

    public double getLongitude() {
        return mLocation.getLongitude();
    }
}

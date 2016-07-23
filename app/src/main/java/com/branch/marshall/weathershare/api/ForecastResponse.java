package com.branch.marshall.weathershare.api;

import com.branch.marshall.weathershare.api.model.CityData;
import com.branch.marshall.weathershare.api.model.ForecastData;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * ApiResponse body for the API call for
 */
public class ForecastResponse implements ApiResponse {
    // MC -- 2016-07-22 -- This shit makes me angry. Why roll up the City info into some object when it's all flat for the current weather response body?
    @SerializedName("city")
    private CityData mCityData;

    @SerializedName("list")
    private List<ForecastData> mList;


    public String getCityName() {
        return mCityData.getName();
    }

    public double getLatitude() {
        return mCityData.getLatitude();
    }

    public double getLongitude() {
        return mCityData.getLongitude();
    }

    public ForecastData getForecastAtIndex(int index) {
        if ((mList != null) && (index >= 0) && (index < mList.size()))
            return mList.get(index);

        return null;
    }
}

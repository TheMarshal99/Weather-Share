package com.branch.marshall.weathershare.api;

import com.google.gson.annotations.SerializedName;

/**
 * Aggregator for information about weather conditions.
 */
public class WeatherConditionData {
    private static final String WEATHER_ICON_URL_BASE = "http://openweathermap.org/img/w/%s.png";

    @SerializedName("main")
    private String mMain;

    @SerializedName("description")
    private String mDescription;

    @SerializedName("icon")
    private String mIcon;

    public String getMain() {
        return mMain;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getIconUrl() {
        return String.format(WEATHER_ICON_URL_BASE, mIcon);
    }
}

package com.branch.marshall.weathershare.util.events;

import com.branch.marshall.weathershare.api.WeatherResponse;

/**
 * Created by marshall on 3/17/16.
 */
public class CityWeatherEvent {
    private WeatherResponse mResponse;

    public CityWeatherEvent(WeatherResponse response) {
        mResponse = response;
    }

    public WeatherResponse getResponse() {
        return mResponse;
    }
}

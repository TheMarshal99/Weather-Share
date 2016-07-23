package com.branch.marshall.weathershare.util.events;

import com.branch.marshall.weathershare.api.CurrentWeatherResponse;

/**
 * Event triggered when the current weather for a city has been obtained from the API.
 */
public class CityWeatherEvent {
    private CurrentWeatherResponse mResponse;

    public CityWeatherEvent(CurrentWeatherResponse response) {
        mResponse = response;
    }

    public CurrentWeatherResponse getResponse() {
        return mResponse;
    }
}

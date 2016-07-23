package com.branch.marshall.weathershare.util.events;

import com.branch.marshall.weathershare.api.ForecastResponse;

/**
 * Event triggered when the 5-day forecast for a city has been obtained from the API.
 */
public class CityForecastEvent {
    private ForecastResponse mResponse;

    public CityForecastEvent(ForecastResponse response) {
        mResponse = response;
    }

    public ForecastResponse getForecast() {
        return mResponse;
    }
}

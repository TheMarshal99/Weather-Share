package com.branch.marshall.weathershare.api;

import com.branch.marshall.weathershare.util.EventManager;
import com.branch.marshall.weathershare.util.events.CityForecastEvent;
import com.branch.marshall.weathershare.util.events.CityWeatherEvent;
import com.branch.marshall.weathershare.util.events.ErrorEvent;
import com.google.gson.Gson;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by marshall on 3/17/16.
 */
public class ApiClient {
    private static final String APP_ID = "ef56be17cd68926bd1dc2afc904c4e76";
    private static final String DEFAULT_UNITS = "imperial";
    private static final int DEFAULT_FORECAST_COUNT = 5;

    private static ApiClient sInstance;
    private static Object lock = new Object();

    private WeatherShareService mService;

    public static ApiClient getInstance() {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null)
                    sInstance = new ApiClient();
            }
        }

        return sInstance;
    }

    private ApiClient() {
        RestAdapter retrofit = new RestAdapter.Builder()
                .setEndpoint("http://api.openweathermap.org")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(new Gson()))
                .build();

        mService = retrofit.create(WeatherShareService.class);
    }

    public void getCurrentWeatherForCity(String city) {
        mService.getCurrentWeatherForCity(city, APP_ID, DEFAULT_UNITS, new Callback<CurrentWeatherResponse>() {
            @Override
            public void success(CurrentWeatherResponse weatherResponse, Response response) {
                EventManager.getInstance().post(new CityWeatherEvent(weatherResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                EventManager.getInstance().post(new ErrorEvent(error.getCause()));
            }
        });
    }

    public void getCurrentWeatherForLocation(double lat, double lon) {
        mService.getCurrentWeatherForLocation(lat, lon, APP_ID, DEFAULT_UNITS, new Callback<CurrentWeatherResponse>() {
            @Override
            public void success(CurrentWeatherResponse weatherResponse, Response response) {
                EventManager.getInstance().post(new CityWeatherEvent(weatherResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                EventManager.getInstance().post(new ErrorEvent(error.getCause()));
            }
        });
    }

    public void getForecastForCity(String city) {
        mService.getForecastForCity(city, APP_ID, DEFAULT_UNITS, DEFAULT_FORECAST_COUNT, new Callback<ForecastResponse>() {
            @Override
            public void success(ForecastResponse forecastResponse, Response response) {
                EventManager.getInstance().post(new CityForecastEvent(forecastResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                EventManager.getInstance().post(new ErrorEvent(error.getCause()));
            }
        });
    }

    public void getForecastForLocation(double lat, double lon) {
        mService.getForecastForLocation(lat, lon, APP_ID, DEFAULT_UNITS, DEFAULT_FORECAST_COUNT, new Callback<ForecastResponse>() {
            @Override
            public void success(ForecastResponse forecastResponse, Response response) {
                EventManager.getInstance().post(new CityForecastEvent(forecastResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                EventManager.getInstance().post(new ErrorEvent(error.getCause()));
            }
        });
    }

    private interface WeatherShareService {
        @GET("/data/2.5/weather")
        void getCurrentWeatherForCity(@Query("q") String cityName, @Query("appid") String appId, @Query("units") String units, Callback<CurrentWeatherResponse> callback);

        @GET("/data/2.5/weather")
        void getCurrentWeatherForLocation(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String appId, @Query("units") String units, Callback<CurrentWeatherResponse> callback);

        @GET("/data/2.5/forecast/daily")
        void getForecastForCity(@Query("q") String cityName, @Query("appid") String appId, @Query("units") String units, @Query("cnt") int limit, Callback<ForecastResponse> callback);

        @GET("/data/2.5/forecast/daily")
        void getForecastForLocation(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String appId, @Query("units") String units, @Query("cnt") int limit, Callback<ForecastResponse> callback);
    }
}

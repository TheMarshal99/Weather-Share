package com.branch.marshall.weathershare.api;

import com.branch.marshall.weathershare.util.EventManager;
import com.branch.marshall.weathershare.util.events.CityWeatherEvent;
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

    public void getWeatherForCity(String city) {
        mService.getWeatherForCity(city, APP_ID, "metric", new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                EventManager.getInstance().post(new CityWeatherEvent(weatherResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                EventManager.getInstance().post(error.getCause());
            }
        });
    }

    private interface WeatherShareService {
        @GET("/data/2.5/weather")
        void getWeatherForCity(@Query("q") String cityName, @Query("appid") String appId, @Query("units") String units, Callback<WeatherResponse> callback);
    }
}

package com.weather.weatherapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("weather")
    Call<Weather> getWeatherData(
            @Query("q") String cityName,
            @Query("appid") String apiKey
    );


    @GET("forecast")
    Call<WeatherForecast> getForecastData(
            @Query("q") String cityName,
            @Query("appid") String apiKey
    );

    // TODO: moze naprawic zdjecia

}
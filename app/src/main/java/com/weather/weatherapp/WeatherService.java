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

    // TODO: updatowac i forecast i weather i zapisywac w storage wszystko, przy kazdym callu i huj, moze naprawic zdjecia, dodac guzik do remove favorite city potem i tak zeby tez usuwalo je ze shared preferences

}
// zapisywanie, detale, usuwanie i inne
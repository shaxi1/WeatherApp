package com.weather.weatherapp;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherApiClient {
    /* example api key */
    private final String api_key = "ca73cc503f58a5b4e8fbd70703351ce8";

    public WeatherApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    }


    public boolean cityIsCorrect(String cityName) {


        return true;
    }
}

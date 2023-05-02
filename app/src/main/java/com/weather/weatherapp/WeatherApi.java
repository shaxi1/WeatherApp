package com.weather.weatherapp;

import java.io.IOException;
import java.io.SyncFailedException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherApi {
    /* example api key */
    private final String API_KEY = "ca73cc503f58a5b4e8fbd70703351ce8";

    private WeatherService weatherService;

    public WeatherApi() {
        this.weatherService = WeatherClient.getRetrofitInstance().create(WeatherService.class);

    }


    // when cod equals 200, it means that the city name is correct, if it equals 404,
    // it means that the city name is incorrect
    public void cityIsCorrect(String cityName, OnCityCheckListener listener) {
        Call<Weather> call = weatherService.getWeatherData(cityName, API_KEY);
        System.out.println("making call");

        call.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                if (response.isSuccessful()) {
                    Weather weather = response.body();
                    if (weather != null) {
                        System.out.println("cod " + weather.getCod());
                        if (weather.getCod().equals("200"))
                            listener.onCityCheckSuccess();
                    } else {
                        System.out.println("Weather is null");
                        listener.onCityCheckFailure();
                    }
                } else {
                    System.out.println("Response was not successful");
                    try {
                        System.out.println(response.errorBody().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    listener.onCityCheckFailure();
                }
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                listener.onCityCheckFailure();
            }
        });
    }



    public String getRawWeatherData(String cityName) {

        return null;
    }
}

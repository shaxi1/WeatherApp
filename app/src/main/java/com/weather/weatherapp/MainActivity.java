package com.weather.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    final String API_KEY = "ca73cc503f58a5b4e8fbd70703351ce8";
    final long ONE_HOUR_IN_MILISECONDS = 3600000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HomeFragment homeFragment = HomeFragment.newInstance();
        DetailsFragment detailsFragment = DetailsFragment.newInstance();
        NextDaysFragment nextDaysFragment = NextDaysFragment.newInstance();
        SettingsFragment settingsFragment = SettingsFragment.newInstance();

        setCurrentFragment(homeFragment);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    setCurrentFragment(homeFragment);
                    break;
                case R.id.nav_details:
                    setCurrentFragment(detailsFragment);
                    break;
                case R.id.nav_settings:
                    setCurrentFragment(settingsFragment);
                    break;
                case R.id.nav_next_days:
                    setCurrentFragment(nextDaysFragment);
                    break;
            }
            return true;
        });

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        boolean isConnected = nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        if (!isConnected) {
            Alerter alerter = new Alerter(this);
            alerter.noInternetConnectionAlert();
        }

        // data refreshing
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dataRefresh();
                SettingsParser settingsParser = new SettingsParser(getApplicationContext());
                long refreshEveryHours = settingsParser.getFrequency();
                handler.postDelayed(this, refreshEveryHours * ONE_HOUR_IN_MILISECONDS);
            }
        };

        // check if any city needs refreshing after when the app was closed
        WeatherService weatherService =  WeatherClient.getRetrofitInstance().create(WeatherService.class);
        SettingsParser settingsParser = new SettingsParser(this);
        WeatherStorage weatherStorage = new WeatherStorage(this);

        String[] favoriteCities = settingsParser.getFavoriteCities();
        Date timeNow = new Date();
        timeNow.setTime(System.currentTimeMillis());
        for (String cityName : favoriteCities) {
            Date lastRefresh = weatherStorage.getLastModifiedWeather(cityName);
            if (lastRefresh != null) {
                long difference = timeNow.getTime() - lastRefresh.getTime();
                if (difference > ONE_HOUR_IN_MILISECONDS * settingsParser.getFrequency()) {
                    refreshCityData(API_KEY, weatherService, cityName);
                }
            }
        }

    }

    public void dataRefresh() {
        WeatherService weatherService =  WeatherClient.getRetrofitInstance().create(WeatherService.class);

        SettingsParser settingsParser = new SettingsParser(this);

        String[] favoriteCities = settingsParser.getFavoriteCities();
        for (String cityName : favoriteCities) {
            refreshCityData(API_KEY, weatherService, cityName);
        }

    }

    private void refreshCityData(String API_KEY, WeatherService weatherService, String cityName) {
        // Weather
        Call<Weather> call = weatherService.getWeatherData(cityName, API_KEY);
        call.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                if (response.isSuccessful()) {
                    Weather weather = response.body();
                    assert weather != null;

                    WeatherStorage weatherStorage = new WeatherStorage(getApplicationContext());
                    weatherStorage.saveCityWeather(cityName, weather);
                }
            }
            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
            }
        });

        // Forecast
        Call<WeatherForecast> callForecast = weatherService.getForecastData(cityName, API_KEY);

        callForecast.enqueue(new Callback<WeatherForecast>() {
            @Override
            public void onResponse(Call<WeatherForecast> call, Response<WeatherForecast> response) {
                if (response.isSuccessful()) {
                    WeatherForecast weatherForecast = response.body();
                    assert weatherForecast != null;

                    WeatherStorage weatherStorage = new WeatherStorage(getApplicationContext());
                    weatherStorage.saveCityForecast(cityName, weatherForecast);
                }
            }
            @Override
            public void onFailure(Call<WeatherForecast> call, Throwable t) {
            }
        });
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, fragment).commit();
    }
}
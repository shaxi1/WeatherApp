package com.weather.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Date;

public class WeatherStorage {
    private static final String PREFS_NAME_PREFIX = "weather_";
    private static final String LAST_MODIFIED_WEATHER_PREFIX = "lastModifiedWeather_";
    private static final String LAST_MODIFIED_FORECAST_PREFIX = "lastModifiedForecast_";
    private static final String WEATHER_PREFIX = "weather_";
    private static final String FORECAST_PREFIX = "forecast_";

    private final Context mContext;
    private final Gson mGson;

    public WeatherStorage(Context context) {
        mContext = context;
        mGson = new Gson();
    }

    public void saveCityWeather(String cityName, Weather weather) {
        SharedPreferences prefs = getPrefs(cityName);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(WEATHER_PREFIX + cityName, mGson.toJson(weather));
        editor.putLong(LAST_MODIFIED_WEATHER_PREFIX + cityName, System.currentTimeMillis());
        editor.apply();
    }

    public Weather loadCityWeather(String cityName) {
        SharedPreferences prefs = getPrefs(cityName);
        String weatherJson = prefs.getString(WEATHER_PREFIX + cityName, null);
        if (weatherJson != null) {
            return mGson.fromJson(weatherJson, Weather.class);
        } else {
            return null;
        }
    }

    public void saveCityForecast(String cityName, WeatherForecast weatherForecast) {
        SharedPreferences prefs = getPrefs(cityName);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(FORECAST_PREFIX + cityName, mGson.toJson(weatherForecast));
        editor.putLong(LAST_MODIFIED_FORECAST_PREFIX + cityName, System.currentTimeMillis());
        editor.apply();
    }

    public WeatherForecast loadCityForecast(String cityName) {
        SharedPreferences prefs = getPrefs(cityName);
        String forecastJson = prefs.getString(FORECAST_PREFIX + cityName, null);
        if (forecastJson != null) {
            return mGson.fromJson(forecastJson, WeatherForecast.class);
        } else {
            return null;
        }
    }

    public Date getLastModifiedWeather(String cityName) {
        SharedPreferences prefs = getPrefs(cityName);
        long lastModified = prefs.getLong(LAST_MODIFIED_WEATHER_PREFIX + cityName, 0);
        return new Date(lastModified);
    }

    public Date getLastModifiedForecast(String cityName) {
        SharedPreferences prefs = getPrefs(cityName);
        long lastModified = prefs.getLong(LAST_MODIFIED_FORECAST_PREFIX + cityName, 0);
        return new Date(lastModified);
    }

    public void deleteCityWeather(String cityName) {
        SharedPreferences prefs = getPrefs(cityName);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(WEATHER_PREFIX + cityName);
        editor.remove(LAST_MODIFIED_WEATHER_PREFIX + cityName);
        editor.apply();
    }

    public void deleteCityForecast(String cityName) {
        SharedPreferences prefs = getPrefs(cityName);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(FORECAST_PREFIX + cityName);
        editor.remove(LAST_MODIFIED_FORECAST_PREFIX + cityName);
        editor.apply();
    }

    private SharedPreferences getPrefs(String cityName) {
        String prefsName = PREFS_NAME_PREFIX + cityName;
        return mContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }
}

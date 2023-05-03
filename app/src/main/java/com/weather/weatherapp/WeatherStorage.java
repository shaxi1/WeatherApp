package com.weather.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Date;

public class WeatherStorage {
    private static final String PREFS_NAME_PREFIX = "weather_";
    private static final String LAST_MODIFIED_PREFIX = "lastModified_";
    //TODO: forecast
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
        editor.putString("weather", mGson.toJson(weather));
        editor.putLong(LAST_MODIFIED_PREFIX + cityName, System.currentTimeMillis());
        editor.apply();
    }

    public Weather loadCityWeather(String cityName) {
        SharedPreferences prefs = getPrefs(cityName);
        String weatherJson = prefs.getString("weather", null);
        if (weatherJson != null) {
            return mGson.fromJson(weatherJson, Weather.class);
        } else {
            return null;
        }
    }

    public Date getLastModified(String cityName) {
        SharedPreferences prefs = getPrefs(cityName);
        long lastModified = prefs.getLong(LAST_MODIFIED_PREFIX + cityName, 0);
        return new Date(lastModified);
    }

    private SharedPreferences getPrefs(String cityName) {
        String prefsName = PREFS_NAME_PREFIX + cityName;
        return mContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }
}

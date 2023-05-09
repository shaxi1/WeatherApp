package com.weather.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;

public class SettingsParser {

    private static final String SHARED_PREFERENCES_NAME = "weather_settings";
    private static final String TEMPERATURE_UNITS_KEY = "temperatureUnits";
    private static final String REFRESH_FREQUENCY_KEY = "refreshFrequency";
    private static final String FAVORITE_CITIES_KEY = "favoriteCities";
    private static final String TIMER_START_TIME_KEY = "timer_elapsed_time";

    private final SharedPreferences sharedPreferences;

    public SettingsParser(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        if (!sharedPreferences.contains(TEMPERATURE_UNITS_KEY)) {
            setUnits("CELSIUS");
        }

        if (!sharedPreferences.contains(REFRESH_FREQUENCY_KEY)) {
            setFrequency(1);
        }

        if (!sharedPreferences.contains(FAVORITE_CITIES_KEY)) {
            setFavoriteCities(new String[]{});
        }
    }

    public void setUnits(String temperatureUnits) {
        sharedPreferences.edit().putString(TEMPERATURE_UNITS_KEY, temperatureUnits).apply();
    }

    public String getUnits() {
        return sharedPreferences.getString(TEMPERATURE_UNITS_KEY, "CELSIUS");
    }

    public void setFrequency(int refreshFrequency) {
        sharedPreferences.edit().putInt(REFRESH_FREQUENCY_KEY, refreshFrequency).apply();
    }

    public int getFrequency() {
        return sharedPreferences.getInt(REFRESH_FREQUENCY_KEY, 1);
    }

    public void setFavoriteCities(String[] favoriteCities) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < favoriteCities.length; i++) {
            stringBuilder.append(favoriteCities[i]);
            if (i < favoriteCities.length - 1) {
                stringBuilder.append(",");
            }
        }

        sharedPreferences.edit().putString(FAVORITE_CITIES_KEY, stringBuilder.toString()).apply();
    }

    public String[] getFavoriteCities() {
        String favoriteCitiesString = sharedPreferences.getString(FAVORITE_CITIES_KEY, "");
        return favoriteCitiesString.split(",");
    }

    public void addCity(String city) {
        if (cityIsAFavoriteCity(city)) return;

        String[] favoriteCities = getFavoriteCities();
        String[] newFavoriteCities = new String[favoriteCities.length + 1];
        System.arraycopy(favoriteCities, 0, newFavoriteCities, 0, favoriteCities.length);
        newFavoriteCities[favoriteCities.length] = city;
        setFavoriteCities(newFavoriteCities);
    }

    private boolean cityIsAFavoriteCity(String city) {
        for (String favoriteCity : getFavoriteCities()) {
            if (favoriteCity.equals(city)) {
                return true;
            }
        }
        return false;
    }

    public void removeCity(String city) {
        if (!cityIsAFavoriteCity(city)) return;

        String[] favoriteCities = getFavoriteCities();
        List<String> newFavoriteCitiesList = new ArrayList<>(Arrays.asList(favoriteCities));
        newFavoriteCitiesList.remove(city);
        String[] newFavoriteCities = newFavoriteCitiesList.toArray(new String[0]);
        setFavoriteCities(newFavoriteCities);
    }

    public void saveStartTime(Timer timer, Context context) {
        long startTime = 0;
        if (timer != null) {
            startTime = SystemClock.elapsedRealtime();
        }
        sharedPreferences.edit().putLong(TIMER_START_TIME_KEY, startTime).apply();
    }

    public long restoreStartTime(Context context) {
        return sharedPreferences.getLong(TIMER_START_TIME_KEY, 0);
    }
}

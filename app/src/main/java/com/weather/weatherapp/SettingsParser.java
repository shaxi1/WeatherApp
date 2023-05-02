package com.weather.weatherapp;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SettingsParser {
    private static final String FILE_NAME = "weather_settings.properties";

    private Properties properties;
    private Context context;

    public SettingsParser(Context context) throws IOException {
        this.context = context;
        properties = new Properties();
        // check if settings file exists if not create it with default values
        loadProperties(context);
        if (properties.isEmpty()) {
            properties.setProperty("temperatureUnits", "CELSIUS");
            properties.setProperty("refreshFrequency", "1");
            properties.setProperty("cities", "");
            saveProperties(context);
        }
    }

    public void setUnits(String selectedUnits) {
        if (!selectedUnits.equals("CELSIUS") && !selectedUnits.equals("FAHRENHEIT") && !selectedUnits.equals("KELVIN"))
            return;

        properties.setProperty("temperatureUnits", selectedUnits);
        saveProperties();
    }

    public void setFrequency(int selectedFrequency) {
        properties.setProperty("refreshFrequency", String.valueOf(selectedFrequency));
        saveProperties();
    }

    public String getUnits() {
        return properties.getProperty("temperatureUnits");
    }

    public int getFrequency() {
        return Integer.parseInt(properties.getProperty("refreshFrequency"));
    }

    private void loadProperties(Context context) throws IOException {
        FileInputStream inputStream = context.openFileInput(FILE_NAME);
        properties.load(inputStream);
        inputStream.close();
    }

    private void saveProperties(Context context) throws IOException {
        FileOutputStream outputStream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
        properties.store(outputStream, null);
        outputStream.close();
    }

    private void saveProperties() {
        try {
            saveProperties(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

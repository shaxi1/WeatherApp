package com.weather.weatherapp;

import android.content.Context;
import android.content.res.Resources;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class SettingsParser {
    private final PropertiesConfiguration config;
    private final BufferedReader reader;
    private final BufferedWriter writer;


    public SettingsParser(Context context) throws ConfigurationException, FileNotFoundException {
        Resources resources = context.getResources();
        this.reader = new BufferedReader(new InputStreamReader(resources.openRawResource(R.raw.weather_settings), StandardCharsets.UTF_8));
        this.writer = new BufferedWriter(new OutputStreamWriter(context.openFileOutput("weather_settings.properties", Context.MODE_PRIVATE), StandardCharsets.UTF_8));
        this.config = new PropertiesConfiguration();
    }

    public int getFrequency() throws ConfigurationException, IOException {
        config.read(reader);
        return config.getInt("refreshFrequency", 1);
    }

    public String getUnits() throws ConfigurationException, IOException {
        config.read(reader);
        return config.getString("temperatureUnits", "CELSIUS");
    }

    public void setFrequency(int frequency) throws ConfigurationException, IOException {
        config.read(reader);

        config.setProperty("refreshFrequency", frequency);
        config.write(writer);
    }

    public void setUnits(String units) throws ConfigurationException, IOException {
        if (!units.equals("CELSIUS") && !units.equals("FAHRENHEIT") && !units.equals("KELVIN"))
            return;

        config.read(reader);

        config.setProperty("temperatureUnits", units);
        config.write(writer);
    }

}

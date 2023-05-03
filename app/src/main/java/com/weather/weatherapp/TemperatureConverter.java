package com.weather.weatherapp;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TemperatureConverter {

    public static double convert(double temperature, String toUnit) {
        switch (toUnit) {
            case "CELSIUS":
                return round(temperature - 273.15, 2);
            case "FAHRENHEIT":
                return round((temperature - 273.15) * 9/5 + 32, 2);
            case "KELVIN":
                return round(temperature, 2);
            default:
                throw new IllegalArgumentException("Invalid unit: " + toUnit);
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

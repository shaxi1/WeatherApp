package com.weather.weatherapp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    @SerializedName("cod")
    private String cod;

    @SerializedName("weather")
    private List<WeatherElement> weather;

    @SerializedName("main")
    private Main main;

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public List<WeatherElement> getWeather() {
        return weather;
    }

    public void setWeather(List<WeatherElement> weather) {
        this.weather = weather;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public static class WeatherElement {
        private String main;
        private String icon;

        public String getMain() {
            return main;
        }

        public void setMain(String main) {
            this.main = main;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }

    public static class Main {
        private Double temp;

        public Double getTemp() {
            return temp;
        }

        public void setTemp(Double temp) {
            this.temp = temp;
        }
    }
}

package com.weather.weatherapp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherForecast implements java.io.Serializable {
    @SerializedName("list")
    private List<Forecast> forecastList;

    public List<Forecast> getForecastList() {
        return forecastList;
    }

    public void setForecastList(List<Forecast> forecastList) {
        this.forecastList = forecastList;
    }

    public static class Forecast {
        @SerializedName("main")
        private MainForecast main;

        @SerializedName("weather")
        private List<Weather> weatherList;

        @SerializedName("dt_txt")
        private String dateTime;

        public MainForecast getMain() {
            return main;
        }

        public void setMain(MainForecast main) {
            this.main = main;
        }

        public List<Weather> getWeatherList() {
            return weatherList;
        }

        public void setWeatherList(List<Weather> weatherList) {
            this.weatherList = weatherList;
        }

        public String getDateTime() {
            return dateTime;
        }

        public void setDateTime(String dateTime) {
            this.dateTime = dateTime;
        }
    }

    public static class MainForecast {
        @SerializedName("temp")
        private double temp;

        public double getTemp() {
            return temp;
        }

        public void setTemp(double temp) {
            this.temp = temp;
        }
    }

    public static class Weather {
        @SerializedName("main")
        private String main;

        public String getMain() {
            return main;
        }

        public void setMain(String main) {
            this.main = main;
        }
    }
}

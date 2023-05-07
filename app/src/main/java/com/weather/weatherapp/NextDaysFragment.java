package com.weather.weatherapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NextDaysFragment extends Fragment {
    private SettingsParser settingsParser;
    private WeatherForecast weatherForecast;
    private String cityName;

    public NextDaysFragment() {
        // Required empty public constructor
    }

    public static NextDaysFragment newInstance() {
        return new NextDaysFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsParser = new SettingsParser(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_next_days, container, false);

        Bundle args = getArguments();
        if (args != null && args.containsKey("weatherForecast")) {
            weatherForecast = new WeatherForecast();
            weatherForecast = (WeatherForecast) args.getSerializable("weatherForecast");
            cityName = args.getString("cityName");
        }

        if (weatherForecast != null && cityName != null && !cityName.isEmpty()) {
            updateViews(weatherForecast, cityName, view);
        } else {
            clearTextViews(view);
        }

        return view;
    }

    private void clearTextViews(View requireView) {
        final int forecastNumber = 5;
        final String textViewIdPrefix = "forecast_";

        for (int i = 0; i < forecastNumber; i++) {
            String textViewId = textViewIdPrefix + (i + 1);
            TextView textView = requireView.findViewById(getResources().getIdentifier(textViewId, "id", requireView.getContext().getPackageName()));
            textView.setText("");
        }
    }

    private void updateViews(WeatherForecast weatherForecast, String selectedCity, View view) {
        final int forecastNumber = 5;
        final String textViewIdPrefix = "forecast_";

        List<WeatherForecast.Forecast> forecastList = weatherForecast.getForecastList();
        for (int i = 0; i < forecastNumber; i++) {
            String textViewId = textViewIdPrefix + (i + 1);
            TextView textView = view.findViewById(getResources().getIdentifier(textViewId, "id", view.getContext().getPackageName()));
            // forecastText: date: city name, main weather, temp, units
            Double temp = forecastList.get(i).getMain().getTemp();
            temp = TemperatureConverter.convert(temp, settingsParser.getUnits());
            String forecastText = forecastList.get(i).getDateTime() + ": " + selectedCity + ", " + forecastList.get(i).getWeatherList().get(0).getMain() + ", " + temp + " " + settingsParser.getUnits();
            textView.setText(forecastText);
        }
    }

}

package com.weather.weatherapp;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsFragment extends Fragment {
    private SettingsParser settingsParser;
    private Weather weather;
    private String cityName;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance() {
        return new DetailsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsParser = new SettingsParser(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        Bundle args = getArguments();
        if (args != null && args.containsKey("weather")) {
            weather = new Weather();
            weather = (Weather) args.getSerializable("weather");
            cityName = args.getString("cityName");
        }

        if (weather != null && cityName != null && !cityName.isEmpty()) {
            updateViews(weather, cityName, view);
        } else {
            clearTextViews(view);
        }

        return view;
    }

    private void clearTextViews(View view) {
        TextView tvCityName = view.findViewById(R.id.city_text);
        tvCityName.setText("");
        TextView tvMainText = view.findViewById(R.id.main_text);
        tvMainText.setText("");
        TextView tvTemperature = view.findViewById(R.id.temp_text);
        tvTemperature.setText("");
        TextView tvHumidity = view.findViewById(R.id.humidity_text);
        tvHumidity.setText("");
        TextView tvWindSpeed = view.findViewById(R.id.wind_speed_text);
        tvWindSpeed.setText("");
        TextView tvVisibility = view.findViewById(R.id.visibility_text);
        tvVisibility.setText("");
        TextView tvPressure = view.findViewById(R.id.pressure_text);
        tvPressure.setText("");
    }

    private void updateViews(Weather weather, String cityName, View view) {
        TextView tvCityName = view.findViewById(R.id.city_text);
        tvCityName.setText(cityName);

        TextView tvMainText = view.findViewById(R.id.main_text);
        tvMainText.setText(weather.getWeather().get(0).getMain());

        TextView tvTemperature = view.findViewById(R.id.temp_text);
        String units = settingsParser.getUnits();
        Double temp = weather.getMain().getTemp();
        temp = TemperatureConverter.convert(temp, units);
        tvTemperature.setText(temp + " " + units);

        TextView tvHumidity = view.findViewById(R.id.humidity_text);
        tvHumidity.setText("Humidity " + weather.getMain().getHumidity() + "%");
        TextView tvWindSpeed = view.findViewById(R.id.wind_speed_text);
        tvWindSpeed.setText("Wind Speed " + weather.getWind().getSpeed() + " m/s");
        TextView tvVisibility = view.findViewById(R.id.visibility_text);
        tvVisibility.setText("Visibility " + weather.getVisibility() + " m");
        TextView tvPressure = view.findViewById(R.id.pressure_text);
        tvPressure.setText("Pressure " + weather.getMain().getPressure() + " hPa");
    }
}

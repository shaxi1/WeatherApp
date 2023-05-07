package com.weather.weatherapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.io.SyncFailedException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private SettingsParser settingsParser;
    private Weather weather;
    private String cityName;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsParser = new SettingsParser(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Bundle args = getArguments();
        if (args != null && args.containsKey("weather")) {
            System.out.println("HomeFragment args not null and contain weather");
            weather = new Weather();
            weather = (Weather) args.getSerializable("weather");
            cityName = args.getString("cityName");
        }

        Button removeCityButton = view.findViewById(R.id.remove_city_button);
        if (weather != null && cityName != null && !cityName.isEmpty()) {
            try {
                System.out.println("HomeFragment Updating views");
                updateViews(weather, cityName, view);
                System.out.println("Making remove button visible");
                removeCityButton.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (weather == null)
                System.out.println("HomeFragment weather is null");

            removeCityButton.setVisibility(View.INVISIBLE);
            clearTextViews(view);
        }

        buttonConfigure(view);

        return view;
    }

    private void buttonConfigure(View view) {
        Button removeFavoriteCity = view.findViewById(R.id.remove_city_button);

        removeFavoriteCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeatherStorage weatherStorage = new WeatherStorage(view.getContext());
                Weather weather = weatherStorage.loadCityWeather(cityName);
                if (weather != null) {
                    weatherStorage.deleteCityWeather(cityName);
                    settingsParser.removeCity(cityName);
                    clearTextViews(view);
                }
            }
        });
    }

    private void clearTextViews(View view) {
        TextView tvCityName = view.findViewById(R.id.city_text);
        tvCityName.setText("");
        TextView tvMainText = view.findViewById(R.id.main_text);
        tvMainText.setText("");
        TextView tvTemperature = view.findViewById(R.id.temp_text);
        tvTemperature.setText("");
        ImageView imageView = new ImageView(view.getContext());
        imageView.findViewById(R.id.weather_icon);
        imageView.setImageResource(0);
    }

    private void updateViews(Weather weather, String cityName, View view) throws IOException {
        TextView tvCityName = view.findViewById(R.id.city_text);
        tvCityName.setText(cityName);

        TextView tvMainText = view.findViewById(R.id.main_text);
        tvMainText.setText(weather.getWeather().get(0).getMain());

        TextView tvTemperature = view.findViewById(R.id.temp_text);
        String units = settingsParser.getUnits();
        Double temp = weather.getMain().getTemp();
        temp = TemperatureConverter.convert(temp, units);
        tvTemperature.setText(temp + " " + units);

        List<Weather.WeatherElement> weatherElements = weather.getWeather();
        if (weatherElements != null && !weatherElements.isEmpty()) {
            Weather.WeatherElement weatherElement = weatherElements.get(0); // retrieve the first WeatherElement object
            String iconUrl = "https://openweathermap.org/img/w/" + weatherElement.getIcon() + ".png";
            System.out.println("iconUrl: " + iconUrl);
            ImageView imageView = view.findViewById(R.id.weather_icon);
            Glide.with(this)
                    .load(iconUrl)
                    .into(imageView);
        }
    }

    private String getIconUrl(String iconCode) {
        return "https://openweathermap.org/img/w/" + iconCode + ".png";
    }


}

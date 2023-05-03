package com.weather.weatherapp;

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

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private SettingsParser settingsParser;

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

        Spinner spinner = view.findViewById(R.id.spinner_favorite_cities);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, settingsParser.getFavoriteCities());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // set onItemSelectedListener for spinner to display the weather for the selected city
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = (String) parent.getItemAtPosition(position);
//                // call the method to display weather for the selected city
                displayWeatherForCity(selectedCity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        // other code for initializing and setting up views

        return view;
    }

    private void displayWeatherForCity(String cityName) {
        final String API_KEY = "ca73cc503f58a5b4e8fbd70703351ce8";
        WeatherService weatherService =  WeatherClient.getRetrofitInstance().create(WeatherService.class);
        Call<Weather> call = weatherService.getWeatherData(cityName, API_KEY);

        call.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                if (response.isSuccessful()) {
                    Weather weather = response.body();
                    // TODO: also update and save forecast
                    updateViews(weather, cityName);
                } else {
                    // handle error
                }
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                // handle error
            }
        });
    }

    private void updateViews(Weather weather, String cityName) {
        TextView tvCityName = requireView().findViewById(R.id.city_text);
        tvCityName.setText(cityName);

        TextView tvMainText = requireView().findViewById(R.id.main_text);
        tvMainText.setText(weather.getWeather().get(0).getMain());

        TextView tvTemperature = requireView().findViewById(R.id.temp_text);
        String units = settingsParser.getUnits();
        Double temp = weather.getMain().getTemp();
        temp = TemperatureConverter.convert(temp, units);
        tvTemperature.setText(temp + " " + units);


        List<Weather.WeatherElement> weatherElements = weather.getWeather();
        if (weatherElements != null && !weatherElements.isEmpty()) {
            Weather.WeatherElement weatherElement = weatherElements.get(0); // retrieve the first WeatherElement object
            String iconUrl = "http://openweathermap.org/img/w/" + weatherElement.getIcon() + ".png";
            System.out.println("iconUrl: " + iconUrl);
            ImageView imageView = requireView().findViewById(R.id.weather_icon);
            Glide.with(this)
                    .load(iconUrl)
                    .into(imageView);
        }
    }

    private String getIconUrl(String iconCode) {
        return "https://openweathermap.org/img/w/" + iconCode + ".png";
    }


}

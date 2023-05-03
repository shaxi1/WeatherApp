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
import android.widget.Spinner;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsFragment extends Fragment {
    private SettingsParser settingsParser;

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

        spinnerConfigure(view);

        return view;
    }

    private void spinnerConfigure(View view) {
        Spinner spinner = view.findViewById(R.id.spinner_favorite_cities);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, settingsParser.getFavoriteCities());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // set onItemSelectedListener for spinner to display the weather for the selected city
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = (String) parent.getItemAtPosition(position);
                if (!isNetworkAvailable()) {
                    WeatherStorage weatherStorage = new WeatherStorage(getContext());
                    Weather weather = weatherStorage.loadCityWeather(selectedCity);

                    Alerter alerter = new Alerter(getContext());
                    if (weather != null) {
                        updateViews(weather, selectedCity);
                        alerter.dataCouldBeOutdated(weatherStorage.getLastModifiedWeather(selectedCity));
                    } else {
                        if (!selectedCity.equals(""))
                            alerter.noOfflineDataSaved();
                    }
                } else {
                    displayWeatherForCity(selectedCity);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
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
                    assert weather != null;

                    WeatherStorage weatherStorage = new WeatherStorage(getContext());
                    weatherStorage.saveCityWeather(cityName, weather);
                    updateViews(weather, cityName);
                }
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                Alerter alerter = new Alerter(requireContext());
                alerter.dataFetchErrorAlert();
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

        TextView tvHumidity = requireView().findViewById(R.id.humidity_text);
        tvHumidity.setText("Humidity " + weather.getMain().getHumidity() + "%");
        TextView tvWindSpeed = requireView().findViewById(R.id.wind_speed_text);
        tvWindSpeed.setText("Wind Speed " + weather.getWind().getSpeed() + " m/s");
        TextView tvVisibility = requireView().findViewById(R.id.visibility_text);
        tvVisibility.setText("Visibility " + weather.getVisibility() + " m");
        TextView tvPressure = requireView().findViewById(R.id.pressure_text);
        tvPressure.setText("Pressure " + weather.getMain().getPressure() + " hPa");
    }
}

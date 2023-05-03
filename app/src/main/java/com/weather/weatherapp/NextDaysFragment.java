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

        spinnerConfigure(view);

        return view;
    }

    private void spinnerConfigure(View view) {
        Spinner spinner = view.findViewById(R.id.spinner_favorite_cities);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, settingsParser.getFavoriteCities());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = (String) parent.getItemAtPosition(position);
                if (!isNetworkAvailable()) {
                    WeatherStorage weatherStorage = new WeatherStorage(requireContext());
                    WeatherForecast weatherForecast = weatherStorage.loadCityForecast(selectedCity);

                    Alerter alerter = new Alerter(requireContext());
                    if (weatherForecast != null) {
                        updateViews(weatherForecast, selectedCity);
                        alerter.dataCouldBeOutdated(weatherStorage.getLastModifiedForecast(selectedCity));
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

    private void displayWeatherForCity(String selectedCity) {
        final String API_KEY = "ca73cc503f58a5b4e8fbd70703351ce8";
        WeatherService weatherService =  WeatherClient.getRetrofitInstance().create(WeatherService.class);
        Call<WeatherForecast> call = weatherService.getForecastData(selectedCity, API_KEY);

        call.enqueue(new Callback<WeatherForecast>() {
            @Override
            public void onResponse(Call<WeatherForecast> call, Response<WeatherForecast> response) {
                if (response.isSuccessful()) {
                    WeatherForecast weatherForecast = response.body();
                    assert weatherForecast != null;

                    WeatherStorage weatherStorage = new WeatherStorage(requireContext());
                    weatherStorage.saveCityForecast(selectedCity, weatherForecast);
                    updateViews(weatherForecast, selectedCity);
                }
            }

            @Override
            public void onFailure(Call<WeatherForecast> call, Throwable t) {
                Alerter alerter = new Alerter(requireContext());
                alerter.dataFetchErrorAlert();
            }
        });
    }

    private void updateViews(WeatherForecast weatherForecast, String selectedCity) {
        final int forecastNumber = 5;
        final String textViewIdPrefix = "forecast_";

        List<WeatherForecast.Forecast> forecastList = weatherForecast.getForecastList();
        for (int i = 0; i < forecastNumber; i++) {
            String textViewId = textViewIdPrefix + (i + 1);
            TextView textView = requireView().findViewById(getResources().getIdentifier(textViewId, "id", requireContext().getPackageName()));
            // forecastText: date: city name, main weather, temp, units
            Double temp = forecastList.get(i).getMain().getTemp();
            temp = TemperatureConverter.convert(temp, settingsParser.getUnits());
            String forecastText = forecastList.get(i).getDateTime() + ": " + selectedCity + ", " + forecastList.get(i).getWeatherList().get(0).getMain() + ", " + temp + " " + settingsParser.getUnits();
            textView.setText(forecastText);
        }
    }


}

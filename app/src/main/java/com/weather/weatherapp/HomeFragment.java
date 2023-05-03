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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

        buttonConfigure(view);
        spinnerConfigure(view);

        return view;
    }

    private void buttonConfigure(View view) {
        Button removeFavoriteCity = view.findViewById(R.id.remove_city_button);
        removeFavoriteCity.setVisibility(View.INVISIBLE);

        removeFavoriteCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinner = requireView().findViewById(R.id.spinner_favorite_cities);
                String selectedCity = (String) spinner.getSelectedItem();
                if (selectedCity.equals("")) {
                    return;
                }

                WeatherStorage weatherStorage = new WeatherStorage(getContext());
                Weather weather = weatherStorage.loadCityWeather(selectedCity);
                if (weather != null) {
                    weatherStorage.deleteCityWeather(selectedCity);
                    settingsParser.removeCity(selectedCity);
                    clearTextViews(view);
                    spinnerConfigure(requireView());
                }
            }
        });
    }

    private void clearTextViews(View view) {
        TextView tvCityName = requireView().findViewById(R.id.city_text);
        tvCityName.setText("");
        TextView tvMainText = requireView().findViewById(R.id.main_text);
        tvMainText.setText("");
        TextView tvTemperature = requireView().findViewById(R.id.temp_text);
        tvTemperature.setText("");
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
                Button removeCityButton = requireView().findViewById(R.id.remove_city_button);
                if (!selectedCity.equals(""))
                    removeCityButton.setVisibility(View.VISIBLE);
                else {
                    removeCityButton.setVisibility(View.INVISIBLE);
                    clearTextViews(requireView());
                }

                if (!isNetworkAvailable()) {
                    WeatherStorage weatherStorage = new WeatherStorage(getContext());
                    Weather weather = weatherStorage.loadCityWeather(selectedCity);

                    Alerter alerter = new Alerter(getContext());
                    if (weather != null) {
                        try {
                            updateViews(weather, selectedCity);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
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
                    try {
                        updateViews(weather, cityName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                Alerter alerter = new Alerter(requireContext());
                alerter.dataFetchErrorAlert();
            }
        });
    }

    private void updateViews(Weather weather, String cityName) throws IOException {
        TextView tvCityName = requireView().findViewById(R.id.city_text);
        tvCityName.setText(cityName);

        TextView tvMainText = requireView().findViewById(R.id.main_text);
        tvMainText.setText(weather.getWeather().get(0).getMain());

        TextView tvTemperature = requireView().findViewById(R.id.temp_text);
        String units = settingsParser.getUnits();
        Double temp = weather.getMain().getTemp();
        temp = TemperatureConverter.convert(temp, units);
        tvTemperature.setText(temp + " " + units);

        // TODO: fix icon loading
        List<Weather.WeatherElement> weatherElements = weather.getWeather();
        if (weatherElements != null && !weatherElements.isEmpty()) {
            Weather.WeatherElement weatherElement = weatherElements.get(0); // retrieve the first WeatherElement object
            String iconUrl = "https://openweathermap.org/img/w/" + weatherElement.getIcon() + ".png";
            System.out.println("iconUrl: " + iconUrl);
            ImageView imageView = requireView().findViewById(R.id.weather_icon);
            Glide.with(this)
                    .load(iconUrl)
                    .into(imageView);
        }
//        List<Weather.WeatherElement> weatherElements = weather.getWeather();
//        if (weatherElements != null && !weatherElements.isEmpty()) {
//            Weather.WeatherElement weatherElement = weatherElements.get(0);
//            ImageView imageView = requireView().findViewById(R.id.weather_icon);
//            String iconUrl = "https://openweathermap.org/img/w/" + weatherElement.getIcon() + ".png";
//            System.out.println("iconUrl: " + iconUrl);
//
//            URL url = new URL(iconUrl);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoInput(true);
//            connection.connect();
//            InputStream input = connection.getInputStream();
//            Bitmap bitmap = BitmapFactory.decodeStream(input);
//            imageView.setImageBitmap(bitmap);
//        }
    }

    private String getIconUrl(String iconCode) {
        return "https://openweathermap.org/img/w/" + iconCode + ".png";
    }


}

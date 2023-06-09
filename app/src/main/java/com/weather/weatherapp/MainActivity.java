package com.weather.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    final String API_KEY = "ca73cc503f58a5b4e8fbd70703351ce8";
    final long ONE_HOUR_IN_MILISECONDS = 3600000;
    private SettingsParser settingsParser;
    private volatile Weather weather;
    private volatile WeatherForecast weatherForecast;
    private volatile String cityName;
    private Timer timer;
    private boolean isForeground;
    private long startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HomeFragment homeFragment = HomeFragment.newInstance();
        DetailsFragment detailsFragment = DetailsFragment.newInstance();
        NextDaysFragment nextDaysFragment = NextDaysFragment.newInstance();
        SettingsFragment settingsFragment = SettingsFragment.newInstance();
        weather = new Weather();
        weatherForecast = new WeatherForecast();
        settingsParser = new SettingsParser(this);
        cityName = "";

        spinnerConfigure();

        Spinner spinner = this.findViewById(R.id.spinner_favorite_cities);
        int spinnerPosition = spinner.getSelectedItemPosition();
        Bundle args = new Bundle();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    System.out.println("home navbar");
                    args.putSerializable("weather", (Serializable) weather);
                    setCurrentFragment(homeFragment, args, cityName);
                    break;
                case R.id.nav_details:
                    args.putSerializable("weather", (Serializable) weather);
                    setCurrentFragment(detailsFragment, args, cityName);
                    break;
                case R.id.nav_settings:
                    setCurrentFragment(settingsFragment, null, cityName);
                    break;
                case R.id.nav_next_days:
                    args.putSerializable("weatherForecast", (Serializable) weatherForecast);
                    setCurrentFragment(nextDaysFragment, args, cityName);
                    break;
            }
            return true;
        });

        if (savedInstanceState != null) {
            weather = (Weather) savedInstanceState.getSerializable("weather");
            weatherForecast = (WeatherForecast) savedInstanceState.getSerializable("weatherForecast");
            cityName = savedInstanceState.getString("cityName");
            bottomNavigationView.setSelectedItemId(savedInstanceState.getInt("selectedItemId"));
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        boolean isConnected = nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        if (!isConnected && savedInstanceState == null) {
            Alerter alerter = new Alerter(this);
            alerter.noInternetConnectionAlert();
        }

        // data refreshing

        // check if any city needs refreshing after when the app was closed
        WeatherService weatherService =  WeatherClient.getRetrofitInstance().create(WeatherService.class);
        SettingsParser settingsParser = new SettingsParser(this);
        WeatherStorage weatherStorage = new WeatherStorage(this);

        String[] favoriteCities = settingsParser.getFavoriteCities();
        Date timeNow = new Date();
        timeNow.setTime(System.currentTimeMillis());
        for (String cityName : favoriteCities) {
            Date lastRefresh = weatherStorage.getLastModifiedWeather(cityName);
            if (lastRefresh != null) {
                long difference = timeNow.getTime() - lastRefresh.getTime();
                if (difference > ONE_HOUR_IN_MILISECONDS * settingsParser.getFrequency()) {
                    refreshCityData(API_KEY, weatherService, cityName);
                }
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isForeground = true;
        recreateTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isForeground = false;
        pauseTimer();
        SettingsParser settingsParser = new SettingsParser(getApplicationContext());
        settingsParser.saveStartTime(timer, this);
    }

    public void pauseTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;

            SettingsParser settingsParser = new SettingsParser(getApplicationContext());
            settingsParser.saveStartTime(timer, this);
        }
    }

    public void recreateTimer() {
        if (isForeground && timer == null) {
            SettingsParser settingsParser = new SettingsParser(getApplicationContext());
            startTime = settingsParser.restoreStartTime(this);

            long elapsedTime = SystemClock.elapsedRealtime() - startTime;
            long refreshEveryHours = settingsParser.getFrequency();
            long period = refreshEveryHours * ONE_HOUR_IN_MILISECONDS;
            long delay = Math.max(0, period - (elapsedTime % period));
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    dataRefresh();
                }
            }, delay, period);
        }
    }

    public void updateSpinner() {
        Spinner spinner = this.findViewById(R.id.spinner_favorite_cities);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, settingsParser.getFavoriteCities());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private boolean isAppInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();
        if (runningProcesses != null && !runningProcesses.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && processInfo.processName.equals(getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("selectedCity", cityName);
        outState.putSerializable("weather", weather);
        outState.putSerializable("weatherForecast", weatherForecast);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        outState.putInt("selectedItemId", bottomNavigationView.getSelectedItemId());
    }

    private void spinnerConfigure() {
        Context context = this;
        Spinner spinner = this.findViewById(R.id.spinner_favorite_cities);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, settingsParser.getFavoriteCities());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // set onItemSelectedListener for spinner to set Weather and WeatherForecast fields
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = (String) parent.getItemAtPosition(position);
                if (!isNetworkAvailable()) {
                    WeatherStorage weatherStorage = new WeatherStorage(context);
                    weather = weatherStorage.loadCityWeather(selectedCity);
                    weatherForecast = weatherStorage.loadCityForecast(selectedCity);
                    cityName = selectedCity;

                    Alerter alerter = new Alerter(context);
                    if (weather != null) {
                        System.out.println("Weather loaded from storage");
                        recreateFragment();

                        alerter.dataCouldBeOutdated(weatherStorage.getLastModifiedWeather(selectedCity));
                    } else {
                        if (!selectedCity.equals(""))
                            alerter.noOfflineDataSaved();
                    }
                } else {
                    // set WeatherForecast and Weather and recreate fragment
                    downloadWeather(selectedCity, context, false);
                    downloadWeatherForecast(selectedCity, context, true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

    }

    private void downloadWeatherForecast(String selectedCity, Context context, boolean recreateFragment) {
        WeatherService weatherService =  WeatherClient.getRetrofitInstance().create(WeatherService.class);
        Call<WeatherForecast> call = weatherService.getForecastData(selectedCity, API_KEY);

        call.enqueue(new Callback<WeatherForecast>() {
            @Override
            public void onResponse(Call<WeatherForecast> call, Response<WeatherForecast> response) {
                if (response.isSuccessful()) {
                    WeatherForecast weatherForecastTemp = response.body();
                    assert weatherForecastTemp != null;

                    WeatherStorage weatherStorage = new WeatherStorage(context);
                    weatherStorage.saveCityForecast(selectedCity, weatherForecastTemp);
                    weatherForecast = weatherForecastTemp;
                    cityName = selectedCity;

                    if (recreateFragment)
                        recreateFragment();
                }
            }

            @Override
            public void onFailure(Call<WeatherForecast> call, Throwable t) {
                Alerter alerter = new Alerter(context);
                alerter.dataFetchErrorAlert();
            }
        });
    }

    private void downloadWeather(String selectedCity, Context context, Boolean recreateFragment) {
        WeatherService weatherService =  WeatherClient.getRetrofitInstance().create(WeatherService.class);
        Call<Weather> call = weatherService.getWeatherData(selectedCity, API_KEY);

        call.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                if (response.isSuccessful()) {
                    Weather weatherTemp = response.body();
                    assert weatherTemp != null;

                    WeatherStorage weatherStorage = new WeatherStorage(context);
                    weatherStorage.saveCityWeather(selectedCity, weatherTemp);
                    weather = weatherTemp;
                    cityName = selectedCity;

                    if (recreateFragment)
                        recreateFragment();
                }
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                Alerter alerter = new Alerter(context);
                alerter.dataFetchErrorAlert();
            }
        });
    }

    private void recreateFragment() {
        System.out.println("recreateFragment");
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(bottomNavigationView.getSelectedItemId());
        if (bottomNavigationView.getSelectedItemId() == R.id.nav_settings) {
            SettingsFragment settingsFragment = SettingsFragment.newInstance();
            setCurrentFragment(settingsFragment, null, cityName);
        } else if (bottomNavigationView.getSelectedItemId() == R.id.nav_next_days) {
            NextDaysFragment nextDaysFragment = NextDaysFragment.newInstance();
            Bundle args = new Bundle();
            args.putSerializable("weatherForecast", (Serializable) weatherForecast);
            setCurrentFragment(nextDaysFragment, args, cityName);
        } else if (bottomNavigationView.getSelectedItemId() == R.id.nav_details) {
            DetailsFragment detailsFragment = DetailsFragment.newInstance();
            Bundle args = new Bundle();
            args.putSerializable("weather", (Serializable) weather);
            setCurrentFragment(detailsFragment, args, cityName);
        } else if (bottomNavigationView.getSelectedItemId() == R.id.nav_home) {
            HomeFragment homeFragment = HomeFragment.newInstance();
            Bundle args = new Bundle();
            args.putSerializable("weather", (Serializable) weather);
            System.out.println("City Name from class field: " + cityName);
            setCurrentFragment(homeFragment, args, cityName);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    public void dataRefresh() {
        WeatherService weatherService =  WeatherClient.getRetrofitInstance().create(WeatherService.class);

        SettingsParser settingsParser = new SettingsParser(this);

        String[] favoriteCities = settingsParser.getFavoriteCities();
        for (String cityName : favoriteCities) {
            refreshCityData(API_KEY, weatherService, cityName);
        }

    }

    private void refreshCityData(String API_KEY, WeatherService weatherService, String cityName) {
        // Weather
        Call<Weather> call = weatherService.getWeatherData(cityName, API_KEY);
        call.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                if (response.isSuccessful()) {
                    Weather weatherTemp = response.body();
                    assert weatherTemp != null;

                    WeatherStorage weatherStorage = new WeatherStorage(getApplicationContext());
                    weatherStorage.saveCityWeather(cityName, weatherTemp);
                    weather = weatherTemp;
                }
            }
            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
            }
        });

        // Forecast
        Call<WeatherForecast> callForecast = weatherService.getForecastData(cityName, API_KEY);

        callForecast.enqueue(new Callback<WeatherForecast>() {
            @Override
            public void onResponse(Call<WeatherForecast> call, Response<WeatherForecast> response) {
                if (response.isSuccessful()) {
                    WeatherForecast weatherForecastTemp = response.body();
                    assert weatherForecastTemp != null;

                    WeatherStorage weatherStorage = new WeatherStorage(getApplicationContext());
                    weatherStorage.saveCityForecast(cityName, weatherForecastTemp);
                    weatherForecast = weatherForecastTemp;
                }
            }
            @Override
            public void onFailure(Call<WeatherForecast> call, Throwable t) {
            }
        });
    }

    private void setCurrentFragment(Fragment fragment, Bundle args, String selectedCity) {
        if (args != null)
            args.putString("cityName", selectedCity);
        System.out.println("setCurrentFragment fksdjhfsdkfshkdfhjkfhjk cityName: " + cityName);

        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, fragment).commit();
    }

}
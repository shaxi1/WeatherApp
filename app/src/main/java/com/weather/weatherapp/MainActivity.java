package com.weather.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HomeFragment homeFragment = HomeFragment.newInstance();
        DetailsFragment detailsFragment = DetailsFragment.newInstance();
        NextDaysFragment nextDaysFragment = NextDaysFragment.newInstance();
        SettingsFragment settingsFragment = SettingsFragment.newInstance();

        setCurrentFragment(homeFragment);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    setCurrentFragment(homeFragment);
                    break;
                case R.id.nav_details:
                    setCurrentFragment(detailsFragment);
                    break;
                case R.id.nav_settings:
                    setCurrentFragment(settingsFragment);
                    break;
                case R.id.nav_next_days:
                    setCurrentFragment(nextDaysFragment);
                    break;
            }
            return true;
        });

        checkInternetConnection();

    }

    private void checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        boolean isConnected = nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        if (!isConnected) {
            noInternetConnectionAlert();
        }
    }

    private void noInternetConnectionAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Data might be outdated")
                .setTitle("No internet connection")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, fragment).commit();
    }
}
package com.weather.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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

        setCurrentFragment(homeFragment);

        //create bottomnavigationview
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home_fragment:
                    setCurrentFragment(homeFragment);
                    break;
                case R.id.details_fragment:
                    setCurrentFragment(detailsFragment);
                    break;
                case R.id.next_days_fragment:
                    setCurrentFragment(nextDaysFragment);
                    break;
            }
            return true;
        });

    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, fragment).commit();
    }
}
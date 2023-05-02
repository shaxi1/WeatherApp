package com.weather.weatherapp;

import android.content.res.Resources;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Arrays;

public class SettingsFragment extends Fragment {

    private Spinner unitsSpinner;
    private Spinner frequencySpinner;
    private SettingsParser settingsParser;


    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsParser = new SettingsParser(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);

        setDefaultSpinnerValues(view);
        configureSaveButton(view);
        configureAddCityFeature(view);

        return view;
    }

    private void configureAddCityFeature(View view) {
        Button addCityButton = view.findViewById(R.id.btnAddCity);
        addCityButton.setOnClickListener(v -> {
            EditText cityEditText = view.findViewById(R.id.etCityName);
            String cityName = cityEditText.getText().toString();
            if (cityName.isEmpty()) {
                cityEditText.setError("City name cannot be empty");
                return;
            }

            // TODO: check if city exists with api
            settingsParser.addCity(cityName);
        });
        // TODO: handle enter button
    }

    private void configureSaveButton(View view) {
        Button saveButton = view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            String selectedUnits = unitsSpinner.getSelectedItem().toString();
            String selectedFrequency = frequencySpinner.getSelectedItem().toString();

            settingsParser.setUnits(selectedUnits);
            settingsParser.setFrequency(Integer.parseInt(selectedFrequency));
        });
    }

    private void setDefaultSpinnerValues(View view) {
        unitsSpinner = view.findViewById(R.id.unitsSpinner);
        frequencySpinner = view.findViewById(R.id.frequencySpinner);

        String units = settingsParser.getUnits();
        int frequency = settingsParser.getFrequency();

        Resources res = getResources();
        String[] unitsArray = res.getStringArray(R.array.units_array);
//            System.out.println(Arrays.toString(unitsArray));
        String[] frequencyArray = res.getStringArray(R.array.frequency_array);

        int unitsPosition = Arrays.asList(unitsArray).indexOf(units);
//            System.out.println(unitsPosition);
        int frequencyPosition = Arrays.asList(frequencyArray).indexOf(String.valueOf(frequency));

        unitsSpinner.setSelection(unitsPosition);
        frequencySpinner.setSelection(frequencyPosition);
    }
}

package com.weather.weatherapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public class Alerter {
    private Context context;
    public Alerter(Context context) {
        this.context = context;
    }

    public void noInternetConnectionAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Data might be outdated")
                .setTitle("No internet connection")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void noInternetConnectionCannotAddCityAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Cannot add city without internet connection")
                .setTitle("No internet connection")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void cityAddedAlert(String cityName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("City " + cityName + " added")
                .setTitle("Success")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void cityNotFoundAlert(String cityName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("City " + cityName + " not found")
                .setTitle("Error")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

package com.zeroapp.zeroqr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class FindGeoLocationActivity extends AppCompatActivity {
    private Boolean requestingLocationPermission = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_geo_location);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(view -> onBackPressed());
        int locationPermissionGranted = checkLocationPermission();
        if (locationPermissionGranted==0) {
            getLocation();
        } else if (locationPermissionGranted==1) {
            locationPermissionDenied();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    private void locationPermissionDenied() {
        new MaterialAlertDialogBuilder(FindGeoLocationActivity.this)
                .setTitle(getString(R.string.request_permission))
                .setMessage(getString(R.string.location_permission_denied_dialog_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    int locationPermissionGranted = checkLocationPermission();
                    if (locationPermissionGranted==1) {
                        Intent intent = new Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null));
                        startActivity(intent);
                        requestingLocationPermission = true;
                    } else {
                        ActivityCompat.requestPermissions(FindGeoLocationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> finish())
                .show();
    }

    private int checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            return 0;
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) {
            return 1;
        } else {
            return 2;
        }
    }

    @SuppressLint("MissingPermission")
    public void getLocation(){
        LocationRequest locationRequest = new LocationRequest.Builder(10000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setIntervalMillis(10000)
                .setMinUpdateIntervalMillis(5000)
                .build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        task.addOnCompleteListener(task1 -> {
            try {
                task1.getResult(ApiException.class);
                FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(FindGeoLocationActivity.this);
                mFusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,new CancellationTokenSource().getToken())
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                getIntent().putExtra("lat",String.valueOf(location.getLatitude()));
                                getIntent().putExtra("long",String.valueOf(location.getLongitude()));
                                setResult(RESULT_OK,getIntent());
                                finish();
                            } else {
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> finish());
            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            resolvable.startResolutionForResult(
                                    FindGeoLocationActivity.this,
                                    2);
                        } catch (IntentSender.SendIntentException | ClassCastException e) {
                            finish();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        finish();
                        break;
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationPermission) {
            requestingLocationPermission=false;
            int locationPermissionGranted = checkLocationPermission();
            if (locationPermissionGranted==0) {
                getLocation();
            } else if (locationPermissionGranted==1) {
                locationPermissionDenied();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==0) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }  else {
                locationPermissionDenied();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    getLocation();
                    break;
                case Activity.RESULT_CANCELED:
                    finish();
                    break;
            }
        }
    }
}
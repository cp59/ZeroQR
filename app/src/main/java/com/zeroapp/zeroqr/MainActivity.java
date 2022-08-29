package com.zeroapp.zeroqr;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fab;
    private Boolean isScannerMode,isHistoryMode,isSettingsMode;
    private Boolean requestingCameraPermission;
    private Boolean requestingReadContactsPermission;
    private Boolean requestingLocationPermission;
    public static Boolean requestingStoragePermission;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private TextInputEditText inputLatitude;
    private TextInputEditText inputLongitude;
    private ProgressDialog locationProgressDialog;
    private GoogleApiClient googleApiClient;
    private Integer orientation;
    protected static final int REQUEST_CHECK_SETTINGS = 2;
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processExtraData();
    }

    @Override
    public void onBackPressed() {
        if (isHistoryMode||isSettingsMode) {
            back();
        } else {
            super.onBackPressed();
        }
    }
    public void back(){
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
        bottomNavigationView.getMenu().getItem(0).setCheckable(false);
        bottomNavigationView.getMenu().getItem(2).setCheckable(false);
        bottomNavigationView.getMenu().getItem(1).setEnabled(false);
        if (!isScannerMode) {
            if (isHistoryMode) {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left).replace(R.id.frameLayout, new GeneratorFragment()).commit();
            } else {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right).replace(R.id.frameLayout, new GeneratorFragment()).commit();
            }

            fab.setImageResource(R.drawable.ic_qr_code_scanner);
            isScannerMode = false;
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScannerFragment()).commit();
            if (isHistoryMode) {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left).replace(R.id.frameLayout, new ScannerFragment()).commit();
            } else {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right).replace(R.id.frameLayout, new ScannerFragment()).commit();
            }
            fab.setImageResource(R.drawable.ic_add);
            isScannerMode = true;
        }
        isHistoryMode=false;
        isSettingsMode=false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        isScannerMode=false;
        isHistoryMode=false;
        isSettingsMode=false;
        orientation=getResources().getConfiguration().orientation;
        requestingCameraPermission=false;
        requestingReadContactsPermission=false;
        requestingLocationPermission=false;
        requestingStoragePermission=false;
        settings = getApplicationContext().getSharedPreferences("ZeroQR", 0);
        editor = settings.edit();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.getMenu().getItem(0).setCheckable(false);
        bottomNavigationView.getMenu().getItem(2).setCheckable(false);
        bottomNavigationView.getMenu().getItem(1).setEnabled(false);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHistoryMode||isSettingsMode) {
                    back();
                } else {
                    if (isScannerMode) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new GeneratorFragment()).commit();
                        fab.setImageResource(R.drawable.ic_qr_code_scanner);
                        isScannerMode = false;
                    } else {
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScannerFragment()).commit();
                        fab.setImageResource(R.drawable.ic_add);
                        isScannerMode = true;
                    }
                }

            }
        });
        locationProgressDialog = new ProgressDialog(MainActivity.this);
        locationProgressDialog.setMessage(getString(R.string.finding_location));
        locationProgressDialog.setCancelable(false);
        googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        googleApiClient.connect();
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if ("com.zeroapp.zeroqr.scan".equals(action)){
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScannerFragment()).commit();
            fab.setImageResource(R.drawable.ic_add);
            isScannerMode = true;
        } else if ("com.zeroapp.zeroqr.create".equals(action)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new GeneratorFragment()).commit();
            fab.setImageResource(R.drawable.ic_qr_code_scanner);
            isScannerMode = false;
        } else if (settings.getInt("startupMode",0)==0) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScannerFragment()).commit();
            fab.setImageResource(R.drawable.ic_add);
            isScannerMode = true;
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new GeneratorFragment()).commit();

            fab.setImageResource(R.drawable.ic_qr_code_scanner);
            isScannerMode = false;
        }
        processExtraData();
    }
    public void processExtraData(){
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Uri data = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                Intent gintent = new Intent(MainActivity.this, GeneratorResultActivity.class);
                gintent.putExtra("content", intent.getStringExtra(Intent.EXTRA_TEXT));
                startActivity(gintent);
            } else if (type.startsWith("image/")) {
                try {
                    InputStream is = getContentResolver().openInputStream(data);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    scanQRImage(bitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void scanQRImage(Bitmap bMap) {
        String content = null;
        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());
        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            content = result.getText();
            Intent srintent = new Intent(this, ResultActivity.class);
            srintent.putExtra("CONTENT", content);
            startActivity(srintent);
        } catch (Exception e) {
            Log.e("QrTest", "Error decoding barcode", e);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.qr_code_not_found))
                    .setNegativeButton(android.R.string.ok, null)
                    .show();
        }
    }
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_history:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right).replace(R.id.frameLayout, new HistoryFragment()).commit();
                fab.setImageResource(R.drawable.ic_arrow_forward);
                bottomNavigationView.getMenu().clear();
                bottomNavigationView.inflateMenu(R.menu.history_bottom_nav_menu);
                bottomNavigationView.getMenu().getItem(0).setCheckable(false);
                bottomNavigationView.getMenu().getItem(2).setCheckable(false);
                bottomNavigationView.getMenu().getItem(1).setEnabled(false);
                isHistoryMode=true;
                return false;
            case R.id.action_settings:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left).replace(R.id.frameLayout, new SettingsFragment()).commit();
                fab.setImageResource(R.drawable.ic_arrow_back);
                bottomNavigationView.getMenu().clear();
                bottomNavigationView.inflateMenu(R.menu.empty_menu);
                isSettingsMode=true;
                return false;
            case R.id.action_refresh_history:
                ((HistoryFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayout)).loadHistory();
                return false;
            case R.id.action_clear_history:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.clear_history_dialog_title))
                        .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((HistoryFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayout)).clearHistory();
                            }
                        })
                        .setNegativeButton(getString(android.R.string.cancel),null)
                        .show();
                return false;
            default:
                return false;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (requestingCameraPermission) {
            requestingCameraPermission = false;
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScannerFragment()).commit();
        } else if (requestingReadContactsPermission){
            requestingReadContactsPermission = false;
            if (ContextCompat.checkSelfPermission(
                    MainActivity.this, Manifest.permission.READ_CONTACTS) ==
                    PackageManager.PERMISSION_GRANTED) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent, 1);
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_CONTACTS)) {
                readContactsPermissionDenied();

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 1);

            }
        } else if (requestingLocationPermission) {
            requestingLocationPermission = false;
            setLocationFromGPS();
        }
    }
    public void locationSetEditText(TextInputEditText latitude,TextInputEditText longitude) {
        inputLatitude = latitude;
        inputLongitude = longitude;
    }
    public void setLocationFromGPS(){
        if (ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(10000 / 2);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            locationProgressDialog.show();
                            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                            mFusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY,new CancellationTokenSource().getToken())
                                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                                        @Override
                                        public void onSuccess(Location location) {
                                            // GPS location can be null if GPS is switched off
                                            if (location != null) {
                                                inputLatitude.setText(String.valueOf(location.getLatitude()));
                                                inputLongitude.setText(String.valueOf(location.getLongitude()));
                                                locationProgressDialog.cancel();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            locationProgressDialog.cancel();
                                            Log.d("MapDemoActivity", "Error trying to get last GPS location");
                                            e.printStackTrace();
                                        }
                                    });
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationPermissionDenied();

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            if (requestCode==0){
                cameraPermissionDenied();
            } else if (requestCode==1) {
                readContactsPermissionDenied();
            } else if (requestCode==2) {
                locationPermissionDenied();
            } else if (requestCode==3) {
                storagePermissionDenied(MainActivity.this);
            }
        } else {
            if (requestCode==0){
                ((ScannerFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayout)).startPreview();
            } else if (requestCode==1) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent, 1);
            } else if (requestCode==2) {
                setLocationFromGPS();
            } else if (requestCode==3) {
                GeneratorResultActivity.saveImage(GeneratorResultActivity.resultBitmapWithLogo, MainActivity.this, "ZeroQR");
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.saved_to_album), Snackbar.LENGTH_LONG).show();
            }
        }
    }
    public byte[] readAsByteArray(FileInputStream ios) throws IOException{
        ByteArrayOutputStream ous = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        }finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
            }
        }
        return ous.toByteArray();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();
                String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
                AssetFileDescriptor fd;
                try {
                    fd = this.getContentResolver().openAssetFileDescriptor(uri, "r");
                    FileInputStream fis = fd.createInputStream();
                    String vcardstring= new String(readAsByteArray(fis));

                    VCard vcard = Ezvcard.parse(vcardstring).first();
                    vcard.getPhotos().clear();
                    Intent intent = new Intent(MainActivity.this,GeneratorResultActivity.class);
                    intent.putExtra("content",Ezvcard.write(vcard).version(VCardVersion.V4_0).go());
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to load Contact", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        } else if(requestCode==REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    locationProgressDialog.show();
                    FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                    mFusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, new CancellationTokenSource().getToken())
                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // GPS location can be null if GPS is switched off
                                    if (location != null) {
                                        inputLatitude.setText(String.valueOf(location.getLatitude()));
                                        inputLongitude.setText(String.valueOf(location.getLongitude()));
                                        locationProgressDialog.cancel();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    locationProgressDialog.cancel();
                                    Log.d("MapDemoActivity", "Error trying to get last GPS location");
                                    e.printStackTrace();
                                }
                            });
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        } else if (requestCode==4){
            if (resultCode==RESULT_OK){
                try {
                    scanQRImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void cameraPermissionDenied(){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.request_permission))
                .setMessage(getString(R.string.camera_permission_denied_dialog_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.grant_permission_from_settings_dialog_title))
                                    .setMessage(getString(R.string.grant_permission_from_settings_dialog_message))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            Intent intent = new Intent(
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", getPackageName(), null));
                                            startActivity(intent);
                                            requestingCameraPermission = true;
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            finish();
                                        }
                                    }).show();
                        } else {
                            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScannerFragment()).commit();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                }).show();
    }
    public void readContactsPermissionDenied(){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.request_permission))
                .setMessage(getString(R.string.read_contacts_permission_denied_dialog_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.grant_permission_from_settings_dialog_title))
                                    .setMessage(getString(R.string.grant_permission_from_settings_dialog_message))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            Intent intent = new Intent(
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", getPackageName(), null));
                                            startActivity(intent);
                                            requestingReadContactsPermission = true;
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                        } else {
                            if (ContextCompat.checkSelfPermission(
                                    MainActivity.this, Manifest.permission.READ_CONTACTS) ==
                                    PackageManager.PERMISSION_GRANTED) {
                                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                                startActivityForResult(contactPickerIntent, 1);
                            } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_CONTACTS)) {
                                readContactsPermissionDenied();

                            } else {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 1);

                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }
    public void locationPermissionDenied(){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.request_permission))
                .setMessage(getString(R.string.location_permission_denied_dialog_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.grant_permission_from_settings_dialog_title))
                                    .setMessage(getString(R.string.grant_permission_from_settings_dialog_message))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            Intent intent = new Intent(
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", getPackageName(), null));
                                            startActivity(intent);
                                            requestingLocationPermission = true;
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                        } else {
                            if (ContextCompat.checkSelfPermission(
                                    MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                                    PackageManager.PERMISSION_GRANTED) {
                                setLocationFromGPS();
                            } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)) {
                                locationPermissionDenied();

                            } else {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);

                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }
    public static void storagePermissionDenied(Activity activity){
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.request_permission))
                .setMessage(activity.getString(R.string.storage_permission_denied_dialog_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new AlertDialog.Builder(activity)
                                    .setTitle(activity.getString(R.string.grant_permission_from_settings_dialog_title))
                                    .setMessage(activity.getString(R.string.grant_permission_from_settings_dialog_message))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            Intent intent = new Intent(
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", activity.getPackageName(), null));
                                            activity.startActivity(intent);
                                            requestingStoragePermission = true;
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                        }
                                    }).show();
                        } else {
                            GeneratorResultActivity.saveImage(GeneratorResultActivity.resultBitmapWithLogo, activity, "ZeroQR");
                            Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.saved_to_album), Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).show();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation!=orientation) {
            orientation=newConfig.orientation;
            if (isScannerMode) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,new ScannerFragment()).commit();
            }
        }
    }

    public void scanFromImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_image));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
        startActivityForResult(chooserIntent, 4);
    }
}
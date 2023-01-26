package com.zeroapp.zeroqr;

import static android.os.ext.SdkExtensions.getExtensionVersion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
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


public class MainActivity extends AppCompatActivity {
    private Boolean requestingCameraPermission = false,requestingReadContactsPermission = false;
    public static Boolean requestingStoragePermission;
    private Integer orientation;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private ActivityResultLauncher<Intent> pickContactLauncher;
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processExtraData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickContactLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK)
            {
                Intent data = result.getData();
                Uri contactData = data.getData();
                Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                cursor.moveToFirst();
                @SuppressLint("Range") String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                cursor.close();
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
                AssetFileDescriptor fd;
                try {
                    fd = getContentResolver().openAssetFileDescriptor(uri, "r");
                    FileInputStream fis = fd.createInputStream();
                    String vCardString= new String(readAsByteArray(fis));
                    fd.close();
                    VCard vcard = Ezvcard.parse(vCardString).first();
                    vcard.getPhotos().clear();
                    Intent intent = new Intent(MainActivity.this,GeneratorResultActivity.class);
                    intent.putExtra("content",Ezvcard.write(vcard).version(VCardVersion.V4_0).go());
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        orientation=getResources().getConfiguration().orientation;
        requestingStoragePermission=false;
        SharedPreferences settings = getApplicationContext().getSharedPreferences("ZeroQR", 0);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onItemSelected);
        Intent intent = getIntent();
        String action = intent.getAction();
        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        try {
                            scanQRImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        if ("com.zeroapp.zeroqr.scan".equals(action)){
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScannerFragment()).commit();
            bottomNavigationView.setSelectedItemId(R.id.action_scan_mode);
        } else if ("com.zeroapp.zeroqr.create".equals(action)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new GeneratorFragment()).commit();
            bottomNavigationView.setSelectedItemId(R.id.action_create_mode);
        } else if (settings.getInt("startupMode",0)==0) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScannerFragment()).commit();
            bottomNavigationView.setSelectedItemId(R.id.action_scan_mode);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new GeneratorFragment()).commit();
            bottomNavigationView.setSelectedItemId(R.id.action_create_mode);
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
        String content;
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
            e.printStackTrace();
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle(getString(R.string.qr_code_not_found))
                    .setNegativeButton(android.R.string.ok, null)
                    .show();
        }
    }
    public boolean onItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan_mode:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScannerFragment()).commit();
                return true;
            case R.id.action_create_mode:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new GeneratorFragment()).commit();
                return true;
            case R.id.action_history:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HistoryFragment()).commit();
                return true;
            case R.id.action_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SettingsFragment()).commit();
                return true;
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
                createContactTypeQRCode();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_CONTACTS)) {
                readContactsPermissionDenied();

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 1);

            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            if (requestCode==0){
                cameraPermissionDenied();
            } else if (requestCode==1) {
                readContactsPermissionDenied();
            } else if (requestCode==3) {
                storagePermissionDenied(MainActivity.this);
            }
        } else {
            if (requestCode==0){
                ((ScannerFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayout)).startPreview();
            } else if (requestCode==1) {
                createContactTypeQRCode();
            } else if (requestCode==3) {
                GeneratorResultActivity.saveImage(GeneratorResultActivity.resultBitmapWithLogo, MainActivity.this, "ZeroQR");
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.saved_to_album), Snackbar.LENGTH_LONG).show();
            }
        }
    }
    public void cameraPermissionDenied(){
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle(getString(R.string.request_permission))
                .setMessage(getString(R.string.camera_permission_denied_dialog_message))
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                        new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle(getString(R.string.grant_permission_from_settings_dialog_title))
                                .setMessage(getString(R.string.grant_permission_from_settings_dialog_message))
                                .setPositiveButton(android.R.string.ok, (dialog1, id1) -> {
                                    dialog1.cancel();
                                    Intent intent = new Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", getPackageName(), null));
                                    startActivity(intent);
                                    requestingCameraPermission = true;
                                })
                                .setNegativeButton(android.R.string.cancel, null).show();
                    } else {
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScannerFragment()).commit();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).show();
    }
    public void readContactsPermissionDenied(){
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle(getString(R.string.request_permission))
                .setMessage(getString(R.string.read_contacts_permission_denied_dialog_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)) {
                        new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle(getString(R.string.grant_permission_from_settings_dialog_title))
                                .setMessage(getString(R.string.grant_permission_from_settings_dialog_message))
                                .setPositiveButton(android.R.string.ok, (dialog1, id1) -> {
                                    dialog1.cancel();
                                    Intent intent = new Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", getPackageName(), null));
                                    startActivity(intent);
                                    requestingReadContactsPermission = true;
                                })
                                .setNegativeButton(android.R.string.cancel, (dialog12, id12) -> dialog12.cancel()).show();
                    } else {
                        if (ContextCompat.checkSelfPermission(
                                MainActivity.this, Manifest.permission.READ_CONTACTS) ==
                                PackageManager.PERMISSION_GRANTED) {
                            createContactTypeQRCode();
                        } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_CONTACTS)) {
                            readContactsPermissionDenied();

                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 1);

                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.cancel()).show();
    }
    public static void storagePermissionDenied(Activity activity){
        new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.request_permission))
                .setMessage(activity.getString(R.string.storage_permission_denied_dialog_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        new MaterialAlertDialogBuilder(activity)
                                .setTitle(activity.getString(R.string.grant_permission_from_settings_dialog_title))
                                .setMessage(activity.getString(R.string.grant_permission_from_settings_dialog_message))
                                .setPositiveButton(android.R.string.ok, (dialog1, id1) -> {
                                    dialog1.cancel();
                                    Intent intent = new Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", activity.getPackageName(), null));
                                    activity.startActivity(intent);
                                    requestingStoragePermission = true;
                                })
                                .setNegativeButton(android.R.string.cancel, (dialog12, id12) -> {

                                }).show();
                    } else {
                        GeneratorResultActivity.saveImage(GeneratorResultActivity.resultBitmapWithLogo, activity, "ZeroQR");
                        Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.saved_to_album), Snackbar.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {

                }).show();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation!=orientation) {
            orientation=newConfig.orientation;
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,new ScannerFragment()).commit();
        }
    }

    public void scanFromImage() {
        if (isPhotoPickerAvailable()) {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        } else {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");
            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_image));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
            ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK)
                {
                    Intent data = result.getData();
                    if (data!=null) {
                        try {
                            scanQRImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            });
            launcher.launch(chooserIntent);
        }
    }

    private boolean isPhotoPickerAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return getExtensionVersion(Build.VERSION_CODES.R) >= 2;
        } else
            return false;
    }

    public void createContactTypeQRCode() {
        pickContactLauncher.launch(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI));
    }
    public byte[] readAsByteArray(FileInputStream ios) throws IOException {
        ByteArrayOutputStream ous = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            int read;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        }finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ous.toByteArray();
    }
}
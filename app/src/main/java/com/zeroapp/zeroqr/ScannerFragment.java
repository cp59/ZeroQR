package com.zeroapp.zeroqr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.transition.MaterialFadeThrough;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;


public class ScannerFragment extends Fragment {
    private CompoundBarcodeView barcodeScannerView;
    private BeepManager beepManager;
    private ImageButton flashlightButton;
    private ImageButton switchCameraButton;
    private Boolean cameraPermissionGranted;
    private Boolean flashlightEnable;
    private SharedPreferences settings;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialFadeThrough());
        setExitTransition(new MaterialFadeThrough());
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        settings = getActivity().getApplicationContext().getSharedPreferences("ZeroQR", 0);
        cameraPermissionGranted=false;
        flashlightEnable=false;
        barcodeScannerView = view.findViewById(R.id.scanner);
        beepManager = new BeepManager(getActivity());
        beepManager.setVibrateEnabled(true);
        CameraSettings settings = barcodeScannerView.getBarcodeView().getCameraSettings();
        settings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        flashlightButton = view.findViewById(R.id.flashlightButton);
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            flashlightButton.setEnabled(false);
            flashlightButton.setImageAlpha(0x3F);
        }
        switchCameraButton = view.findViewById(R.id.switchCameraButton);
        ImageButton scanFromImageButton = view.findViewById(R.id.scanFromImage);
        flashlightButton.setOnClickListener(v -> {
            if (flashlightEnable){
                barcodeScannerView.setTorchOff();
                flashlightButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_on));
                flashlightEnable=false;
            } else{
                barcodeScannerView.setTorchOn();
                flashlightButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off));
                flashlightEnable=true;
            }
        });
        switchCameraButton.setOnClickListener(v -> {
            if(barcodeScannerView.getBarcodeView().isPreviewActive()) {
                barcodeScannerView.pause();
            }
            if (settings.getRequestedCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                settings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
                switchCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera_rear));
            } else {
                settings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
                switchCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera_front));
            }
            barcodeScannerView.getBarcodeView().setCameraSettings(settings);
            barcodeScannerView.resume();
        });
        scanFromImageButton.setOnClickListener(v -> ((MainActivity)getActivity()).scanFromImage());
        barcodeScannerView.decodeSingle(this::onDecoded);
        if (ContextCompat.checkSelfPermission(
                    getContext(), Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                cameraPermissionGranted=true;
                barcodeScannerView.resume();

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.CAMERA)) {
            ((MainActivity)getActivity()).cameraPermissionDenied();

        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 0);

        }
    }
    public void onDecoded(BarcodeResult result) {
        Intent intent = new Intent(getActivity(),ResultActivity.class);
        intent.putExtra("CONTENT",result.getText());
        beepManager.setVibrateEnabled(settings.getBoolean("vibrate",true));
        beepManager.setBeepEnabled(settings.getBoolean("beep",true));
        if (settings.getBoolean("copy",false)){
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("text label",result.getText());
            clipboard.setPrimaryClip(clip);
        }
        beepManager.playBeepSoundAndVibrate();
        startActivity(intent);
    }
    public void startPreview(){
        barcodeScannerView.resume();
        barcodeScannerView.decodeSingle(this::onDecoded);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (cameraPermissionGranted) {
            startPreview();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        barcodeScannerView.pause();
    }
}
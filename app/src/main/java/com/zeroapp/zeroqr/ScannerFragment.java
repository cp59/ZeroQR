package com.zeroapp.zeroqr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;


public class ScannerFragment extends Fragment {
    private CompoundBarcodeView barcodeScannerView;
    private CaptureManager captureManager;
    private BeepManager beepManager;
    private ImageButton flashlightButton,switchCameraButton,helpButton,scanFromImageButton;
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
        if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)==false) {
            flashlightButton.setEnabled(false);
            flashlightButton.setImageAlpha(0x3F);
        }
        switchCameraButton = view.findViewById(R.id.switchCameraButton);
        scanFromImageButton = view.findViewById(R.id.scanFromImage);
        helpButton = view.findViewById(R.id.scanHelpButton);
        flashlightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flashlightEnable){
                    barcodeScannerView.setTorchOff();
                    flashlightButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_on));
                    flashlightEnable=false;
                } else{
                    barcodeScannerView.setTorchOn();
                    flashlightButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off));
                    flashlightEnable=true;
                }
            }
        });
        switchCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });
        scanFromImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).scanFromImage();
            }
        });
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialTapTargetPrompt.Builder(getActivity())
                        .setTarget(R.id.switchCameraButton)
                        .setPrimaryText(getString(R.string.switch_camera))
                        .setSecondaryText(getString(R.string.switch_camera_help_text))
                        .setFocalColour(Color.TRANSPARENT)
                        .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                        {
                            @Override
                            public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                            {
                                if (state == MaterialTapTargetPrompt.STATE_DISMISSED||state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)
                                {
                                    new MaterialTapTargetPrompt.Builder(getActivity())
                                            .setTarget(R.id.scanFromImage)
                                            .setPrimaryText(getString(R.string.scan_from_image))
                                            .setFocalColour(Color.TRANSPARENT)
                                            .setSecondaryText(getString(R.string.scan_from_image_help_text))
                                            .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                                            {
                                                @Override
                                                public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                                                {
                                                    if (state == MaterialTapTargetPrompt.STATE_DISMISSED||state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)
                                                    {
                                                        new MaterialTapTargetPrompt.Builder(getActivity())
                                                                .setTarget(R.id.flashlightButton)
                                                                .setPrimaryText(getString(R.string.flashlight))
                                                                .setFocalColour(Color.TRANSPARENT)
                                                                .setSecondaryText(getString(R.string.flashlight_help_text))
                                                                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                                                                {
                                                                    @Override
                                                                    public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                                                                    {
                                                                        if (state == MaterialTapTargetPrompt.STATE_DISMISSED||state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)
                                                                        {

                                                                        }
                                                                    }
                                                                })
                                                                .show();
                                                    }
                                                }
                                            })
                                            .show();
                                }
                            }
                        })
                        .show();
            }
        });
        barcodeScannerView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                onDecoded(result);
            }
        });
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
        barcodeScannerView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                onDecoded(result);
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        if (cameraPermissionGranted==true) {
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
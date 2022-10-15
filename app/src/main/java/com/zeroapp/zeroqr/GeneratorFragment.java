package com.zeroapp.zeroqr;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;



public class GeneratorFragment extends Fragment {
    private Integer encryptionType;
    private CoordinatorLayout maskBackground;
    private String ZeroAppHostURL = "https://zeroapp.tk";
    private String QuickURLHostURL = ZeroAppHostURL+"/qurl";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        maskBackground=view.findViewById(R.id.maskBackground);
        settings = getActivity().getApplicationContext().getSharedPreferences("ZeroQR", 0);
        editor = settings.edit();
        CardView cardViewCreateText = view.findViewById(R.id.create_text);
        cardViewCreateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(),GenerateActivity.class);
                i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.PLAIN_TEXT);
                startActivity(i);
            }
        });
        CardView cardViewCreateURL = view.findViewById(R.id.create_url);
        cardViewCreateURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),GenerateActivity.class);
                i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.URL);
                startActivity(i);
            }
        });
        CardView cardViewCreateEmail = view.findViewById(R.id.create_email);
        cardViewCreateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),GenerateActivity.class);
                i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.EMAIL);
                startActivity(i);
            }
        });
        CardView cardViewCreatePhone = view.findViewById(R.id.create_phone);
        cardViewCreatePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),GenerateActivity.class);
                i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.PHONE_NUMBER);
                startActivity(i);
            }
        });        CardView cardViewCreateContact = view.findViewById(R.id.create_contact);
        cardViewCreateContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(
                        getContext(), Manifest.permission.READ_CONTACTS) ==
                        PackageManager.PERMISSION_GRANTED) {
                    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    getActivity().startActivityForResult(contactPickerIntent, 1);
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_CONTACTS)) {
                    ((MainActivity) getActivity()).readContactsPermissionDenied();

                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 1);

                }
            }
        });
        CardView cardViewCreateSMS = view.findViewById(R.id.create_sms);
        cardViewCreateSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(),GenerateActivity.class);
                i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.SMS);
                startActivity(i);
            }
        });
        CardView cardViewCreateLocation = view.findViewById(R.id.create_location);
        cardViewCreateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(),GenerateActivity.class);
                i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.GEOLOCATION);
                startActivity(i);
            }
        });
        CardView cardViewCreateCalendar = view.findViewById(R.id.create_calendar);
        cardViewCreateCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),GenerateActivity.class);
                i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.CALENDAR_EVENT);
                startActivity(i);
            }
        });
        CardView cardViewCreateWiFi = view.findViewById(R.id.create_wifi);
        cardViewCreateWiFi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(),GenerateActivity.class);
                i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.WIFI);
                startActivity(i);
            }
        });
        CardView cardViewCreateApplication = view.findViewById(R.id.create_application);
        cardViewCreateApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(),AppToPlayStoreURLActivity.class));
            }
        });

    }
}
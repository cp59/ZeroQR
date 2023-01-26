package com.zeroapp.zeroqr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.transition.MaterialFadeThrough;


public class GeneratorFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CardView cardViewCreateText = view.findViewById(R.id.create_text);
        cardViewCreateText.setOnClickListener(view1 -> {
            Intent i = new Intent(getContext(),GenerateActivity.class);
            i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.PLAIN_TEXT);
            startActivity(i);
        });
        CardView cardViewCreateURL = view.findViewById(R.id.create_url);
        cardViewCreateURL.setOnClickListener(v -> {
            Intent i = new Intent(getContext(),GenerateActivity.class);
            i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.URL);
            startActivity(i);
        });
        CardView cardViewCreateEmail = view.findViewById(R.id.create_email);
        cardViewCreateEmail.setOnClickListener(v -> {
            Intent i = new Intent(getContext(),GenerateActivity.class);
            i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.EMAIL);
            startActivity(i);
        });
        CardView cardViewCreatePhone = view.findViewById(R.id.create_phone);
        cardViewCreatePhone.setOnClickListener(v -> {
            Intent i = new Intent(getContext(),GenerateActivity.class);
            i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.PHONE_NUMBER);
            startActivity(i);
        });        CardView cardViewCreateContact = view.findViewById(R.id.create_contact);
        cardViewCreateContact.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    getContext(), Manifest.permission.READ_CONTACTS) ==
                    PackageManager.PERMISSION_GRANTED) {
                ((MainActivity) getActivity()).createContactTypeQRCode();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_CONTACTS)) {
                ((MainActivity) getActivity()).readContactsPermissionDenied();

            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 1);

            }
        });
        CardView cardViewCreateSMS = view.findViewById(R.id.create_sms);
        cardViewCreateSMS.setOnClickListener(view12 -> {
            Intent i = new Intent(getContext(),GenerateActivity.class);
            i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.SMS);
            startActivity(i);
        });
        CardView cardViewCreateLocation = view.findViewById(R.id.create_location);
        cardViewCreateLocation.setOnClickListener(view13 -> {
            Intent i = new Intent(getContext(),GenerateActivity.class);
            i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.GEOLOCATION);
            startActivity(i);
        });
        CardView cardViewCreateCalendar = view.findViewById(R.id.create_calendar);
        cardViewCreateCalendar.setOnClickListener(v -> {
            Intent i = new Intent(getContext(),GenerateActivity.class);
            i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.CALENDAR_EVENT);
            startActivity(i);
        });
        CardView cardViewCreateWiFi = view.findViewById(R.id.create_wifi);
        cardViewCreateWiFi.setOnClickListener(view14 -> {
            Intent i = new Intent(getContext(),GenerateActivity.class);
            i.putExtra("CONTENT_TYPE",ContentDecoder.ContentType.WIFI);
            startActivity(i);
        });
        CardView cardViewCreateApplication = view.findViewById(R.id.create_application);
        cardViewCreateApplication.setOnClickListener(view15 -> startActivity(new Intent(getContext(),AppToPlayStoreURLActivity.class)));

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialFadeThrough());
        setExitTransition(new MaterialFadeThrough());
    }


}
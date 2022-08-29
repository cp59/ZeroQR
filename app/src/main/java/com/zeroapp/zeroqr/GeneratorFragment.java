package com.zeroapp.zeroqr;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;



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
        // Inflate the layout for this fragment
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
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.create_plain_text_bottom_sheet_dialog_layout);
                bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                Button createButton = bottomSheetDialog.findViewById(R.id.create_button);
                Button closeButton = bottomSheetDialog.findViewById(R.id.cancel_button);
                TextInputEditText contentInput = bottomSheetDialog.findViewById(R.id.input_content);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.cancel();
                    }
                });
                createButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (contentInput.getText().toString().isEmpty() == true) {
                            new AlertDialog.Builder(getContext())
                                    .setTitle(getString(R.string.require_content))
                                    .setNegativeButton(android.R.string.ok, null)
                                    .show();
                        } else {
                            Intent intent = new Intent(getContext(), GeneratorResultActivity.class);
                            intent.putExtra("content", contentInput.getText().toString());
                            startActivity(intent);
                            bottomSheetDialog.cancel();
                        }
                    }
                });
                bottomSheetDialog.show();
            }
        });
        CardView cardViewCreateURL = view.findViewById(R.id.create_url);
        cardViewCreateURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.create_url_bottom_sheet_dialog_layout);
                bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                Button createButton = bottomSheetDialog.findViewById(R.id.create_button);
                Button closeButton = bottomSheetDialog.findViewById(R.id.cancel_button);
                Button shortenURLButton = bottomSheetDialog.findViewById(R.id.shorten_url);
                TextInputEditText contentInput = bottomSheetDialog.findViewById(R.id.input_url);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.cancel();
                    }
                });
                createButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (contentInput.getText().toString().isEmpty() == true) {
                            new AlertDialog.Builder(getContext())
                                    .setTitle(getString(R.string.require_url))
                                    .setNegativeButton(android.R.string.ok, null)
                                    .show();
                        } else {
                            Intent intent = new Intent(getContext(), GeneratorResultActivity.class);
                            intent.putExtra("content", contentInput.getText().toString());
                            startActivity(intent);
                            bottomSheetDialog.cancel();
                        }
                    }
                });
                shortenURLButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder shortenURLAlertDialogBuilder = new AlertDialog.Builder(getContext());
                        View shortenURLLayout = getLayoutInflater().inflate(R.layout.shorten_url_dialog_layout,null);
                        shortenURLAlertDialogBuilder.setView(shortenURLLayout);
                        TextInputLayout customIDInputLayout = shortenURLLayout.findViewById(R.id.inputlayout_custom_id);
                        TextInputEditText inputURL = shortenURLLayout.findViewById(R.id.input_url);
                        TextInputEditText inputCustomID = shortenURLLayout.findViewById(R.id.input_custom_id);
                        inputURL.setText(contentInput.getText().toString());
                        Button shortenURLButton = shortenURLLayout.findViewById(R.id.create_button);
                        Button closeShortenDialogButton = shortenURLLayout.findViewById(R.id.cancel_button);
                        CheckBox customIDCheckbox = shortenURLLayout.findViewById(R.id.customIDCheckbox);
                        customIDCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if (b) {
                                    customIDInputLayout.setVisibility(View.VISIBLE);
                                } else {
                                    customIDInputLayout.setVisibility(View.GONE);
                                }
                            }
                        });
                        AlertDialog shortenURLAlertDialog = shortenURLAlertDialogBuilder.create();
                        shortenURLButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (inputURL.getText().toString().isEmpty()) {
                                    new AlertDialog.Builder(getContext())
                                            .setTitle(getString(R.string.require_url))
                                            .setNegativeButton(android.R.string.ok, null)
                                            .show();
                                } else {
                                    if (customIDCheckbox.isChecked() && inputCustomID.getText().toString().isEmpty()) {

                                        new AlertDialog.Builder(getContext())
                                                .setTitle(getString(R.string.require_custom_id))
                                                .setNegativeButton(android.R.string.ok, null)
                                                .show();
                                    } else {
                                        if (!URLUtil.isValidUrl(inputURL.getText().toString())) {
                                            new AlertDialog.Builder(getContext())
                                                    .setTitle(getString(R.string.invalid_url))
                                                    .setNegativeButton(android.R.string.ok, null)
                                                    .show();
                                        } else {

                                            ProgressDialog shortenURLProgressDialog = new ProgressDialog(getContext());
                                            shortenURLProgressDialog.setMessage(getString(R.string.shortening_url));
                                            shortenURLProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                            shortenURLProgressDialog.setCancelable(false);
                                            shortenURLProgressDialog.show();
                                            RequestQueue queue = Volley.newRequestQueue(getContext());
                                            String url = "";
                                            if (customIDCheckbox.isChecked()) {
                                                url = QuickURLHostURL + "/create?format=json&url=" + inputURL.getText().toString() + "&customID=" + inputCustomID.getText().toString();
                                            } else {
                                                url = QuickURLHostURL + "/create?format=json&url=" + inputURL.getText().toString();
                                            }
                                            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                                    new Response.Listener<String>() {
                                                        @Override
                                                        public void onResponse(String response) {
                                                            JSONObject shortURLResp = null;
                                                            try {
                                                                shortURLResp = new JSONObject(response);
                                                                String status = shortURLResp.getString("status");
                                                                ;
                                                                if (status.equals("success")) {
                                                                    contentInput.setText(shortURLResp.getString("surl"));
                                                                    shortenURLAlertDialog.cancel();
                                                                    shortenURLProgressDialog.dismiss();
                                                                } else if (status.equals("error") && shortURLResp.getInt("errorID") == 1) {
                                                                    shortenURLProgressDialog.dismiss();
                                                                    new AlertDialog.Builder(getContext())
                                                                            .setTitle(getString(R.string.custom_id_already_used_dialog_title))
                                                                            .setMessage(getString(R.string.custom_id_already_used_dialog_message))
                                                                            .setNegativeButton(android.R.string.ok, null)
                                                                            .show();
                                                                } else {
                                                                    shortenURLProgressDialog.dismiss();
                                                                    new AlertDialog.Builder(getContext())
                                                                            .setTitle(getString(R.string.unable_to_shorten_url_dialog_title))
                                                                            .setMessage(getString(R.string.unable_to_shorten_url_dialog_message))
                                                                            .setNegativeButton(android.R.string.ok, null)
                                                                            .show();
                                                                }

                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                                shortenURLProgressDialog.dismiss();
                                                                new AlertDialog.Builder(getContext())
                                                                        .setTitle(getString(R.string.unable_to_shorten_url_dialog_title))
                                                                        .setMessage(getString(R.string.unable_to_shorten_url_dialog_message))
                                                                        .setNegativeButton(android.R.string.ok, null)
                                                                        .show();
                                                            }
                                                        }
                                                    }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    shortenURLProgressDialog.dismiss();
                                                    new AlertDialog.Builder(getContext())
                                                            .setTitle(getString(R.string.unable_to_shorten_url_dialog_title))
                                                            .setMessage(getString(R.string.unable_to_shorten_url_dialog_message))
                                                            .setNegativeButton(android.R.string.ok, null)
                                                            .show();
                                                }
                                            });
                                            queue.add(stringRequest);
                                        }
                                    }
                                }
                            }
                        });
                        closeShortenDialogButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                shortenURLAlertDialog.cancel();
                            }
                        });
                        shortenURLAlertDialog.show();
                        if (settings.getBoolean("firstTimeShortenURL",true)) {
                            new AlertDialog.Builder(getContext())
                                    .setTitle(getString(R.string.first_time_shorten_url_dialog_title))
                                    .setMessage(getString(R.string.first_time_shorten_url_dialog_message))
                                    .setPositiveButton(getString(android.R.string.ok), null)
                                    .show();
                            editor.putBoolean("firstTimeShortenURL",false);
                            editor.apply();
                        }
                    }
                });
                bottomSheetDialog.show();
            }
        });
        CardView cardViewCreateEmail = view.findViewById(R.id.create_email);
        cardViewCreateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.create_email_bottom_sheet_dialog_layout);
                bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                Button createButton = bottomSheetDialog.findViewById(R.id.create_button);
                Button closeButton = bottomSheetDialog.findViewById(R.id.cancel_button);
                TextInputEditText inputAddress = bottomSheetDialog.findViewById(R.id.input_address);
                TextInputEditText inputSubject = bottomSheetDialog.findViewById(R.id.input_subject);
                TextInputEditText inputBody = bottomSheetDialog.findViewById(R.id.input_body);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.cancel();
                    }
                });
                createButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String content = "mailto:";
                        content += inputAddress.getText().toString();
                        content += "?";
                        if (!inputSubject.getText().toString().isEmpty()) {
                            content += "subject=";
                            content += inputSubject.getText().toString();
                            content += "&";
                        }
                        if (!inputBody.getText().toString().isEmpty()) {
                            content += "body=";
                            content += inputBody.getText().toString();
                        }
                        Intent intent = new Intent(getContext(), GeneratorResultActivity.class);
                        intent.putExtra("content", content);
                        startActivity(intent);
                        bottomSheetDialog.cancel();
                    }
                });
                bottomSheetDialog.show();
            }
        });
        CardView cardViewCreatePhone = view.findViewById(R.id.create_phone);
        cardViewCreatePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.create_telephone_bottom_sheet_dialog_layout);
                bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                Button createButton = bottomSheetDialog.findViewById(R.id.create_button);
                Button closeButton = bottomSheetDialog.findViewById(R.id.cancel_button);
                TextInputEditText inputTelephoneNumbers = bottomSheetDialog.findViewById(R.id.input_telephone_numbers);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.cancel();
                    }
                });
                createButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (inputTelephoneNumbers.getText().toString().isEmpty() == true) {
                            new AlertDialog.Builder(getContext())
                                    .setTitle(getString(R.string.require_telephone_numbers))
                                    .setNegativeButton(android.R.string.ok, null)
                                    .show();
                        } else {
                            Intent intent = new Intent(getContext(), GeneratorResultActivity.class);
                            intent.putExtra("content", "tel:" + inputTelephoneNumbers.getText().toString());
                            startActivity(intent);
                            bottomSheetDialog.cancel();
                        }
                    }
                });
                bottomSheetDialog.show();
            }
        });
        CardView cardViewCreateContact = view.findViewById(R.id.create_contact);
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
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.create_sms_bottom_sheet_dialog_layout);
                bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                Button createButton = bottomSheetDialog.findViewById(R.id.create_button);
                Button closeButton = bottomSheetDialog.findViewById(R.id.cancel_button);
                TextInputEditText inputNumber = bottomSheetDialog.findViewById(R.id.input_number);
                TextInputEditText inputMessage = bottomSheetDialog.findViewById(R.id.input_message);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.cancel();
                    }
                });
                createButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String content = "smsto:";
                        content += inputNumber.getText().toString();
                        if (inputMessage.getText().toString() != "") {
                            content += ":";
                            content += inputMessage.getText().toString();
                        }
                        Intent intent = new Intent(getContext(), GeneratorResultActivity.class);
                        intent.putExtra("content", content);
                        startActivity(intent);
                        bottomSheetDialog.cancel();
                    }
                });
                bottomSheetDialog.show();
            }
        });
        CardView cardViewCreateLocation = view.findViewById(R.id.create_location);
        cardViewCreateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.create_location_bottom_sheet_dialog_layout);
                bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                Button createButton = bottomSheetDialog.findViewById(R.id.create_button);
                Button closeButton = bottomSheetDialog.findViewById(R.id.cancel_button);
                Button getCurrentGPSLocationButton = bottomSheetDialog.findViewById(R.id.get_current_gps_location_button);
                TextInputEditText inputLatitude = bottomSheetDialog.findViewById(R.id.input_latitude);
                TextInputEditText inputLongitude = bottomSheetDialog.findViewById(R.id.input_longitude);
                ((MainActivity)getActivity()).locationSetEditText(inputLatitude,inputLongitude);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.cancel();
                    }
                });
                createButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (inputLatitude.getText().toString().isEmpty() == true) {
                            new AlertDialog.Builder(getContext())
                                    .setTitle(getString(R.string.require_latitude))
                                    .setNegativeButton(android.R.string.ok, null)
                                    .show();
                        } else if (inputLongitude.getText().toString().isEmpty() == true) {
                            new AlertDialog.Builder(getContext())
                                    .setTitle(getString(R.string.require_longitude))
                                    .setNegativeButton(android.R.string.ok, null)
                                    .show();
                        } else {
                            String content = "geo:";
                            content += inputLatitude.getText().toString();
                            content += ",";
                            content += inputLongitude.getText().toString();
                            Intent intent = new Intent(getContext(), GeneratorResultActivity.class);
                            intent.putExtra("content", content);
                            startActivity(intent);
                            bottomSheetDialog.cancel();
                        }
                    }
                });

                getCurrentGPSLocationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity)getActivity()).setLocationFromGPS();
                    }
                });
                bottomSheetDialog.show();
            }
        });
        CardView cardViewCreateCalendar = view.findViewById(R.id.create_calendar);
        cardViewCreateCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] dtStart = {""};
                final String[] dtEnd = {""};
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.create_calendar_bottom_sheet_dialog_layout);
                bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                Button createButton = bottomSheetDialog.findViewById(R.id.create_button);
                Button closeButton = bottomSheetDialog.findViewById(R.id.cancel_button);
                TextInputEditText inputTitle = bottomSheetDialog.findViewById(R.id.input_title);
                TextInputEditText inputStartTime = bottomSheetDialog.findViewById(R.id.input_start_time);
                TextInputEditText inputEndTime = bottomSheetDialog.findViewById(R.id.input_end_time);
                TextInputEditText inputLocation = bottomSheetDialog.findViewById(R.id.input_location);
                TextInputEditText inputDescription = bottomSheetDialog.findViewById(R.id.input_description);
                Date currentDate = Calendar.getInstance().getTime();
                SimpleDateFormat selectDate = new SimpleDateFormat("yyyyMMdd'T'HHmm00'Z'");
                selectDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                dtStart[0] = selectDate.format(currentDate);
                dtEnd[0] = selectDate.format(currentDate);
                inputStartTime.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(currentDate));
                inputStartTime.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(currentDate));
                inputEndTime.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                inputStartTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new SingleDateAndTimePickerDialog.Builder(getContext())
                                .minutesStep(1)
                                .displayYears(true)
                                .bottomSheet()
                                .displayListener(new SingleDateAndTimePickerDialog.DisplayListener() {
                                    @Override
                                    public void onDisplayed(SingleDateAndTimePicker picker) {
                                        maskBackground.setVisibility(View.VISIBLE);
                                        bottomSheetDialog.hide();
                                    }

                                    @Override
                                    public void onClosed(SingleDateAndTimePicker picker) {
                                        bottomSheetDialog.show();
                                        maskBackground.setVisibility(View.GONE);
                                    }
                                })
                                .title(getString(R.string.start_time))
                                .listener(new SingleDateAndTimePickerDialog.Listener() {
                                    @Override
                                    public void onDateSelected(Date date) {
                                        SimpleDateFormat selectDate = new SimpleDateFormat("yyyyMMdd'T'HHmm00'Z'");
                                        selectDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                                        dtStart[0] = selectDate.format(date);
                                        inputStartTime.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(date));
                                        bottomSheetDialog.show();
                                        maskBackground.setVisibility(View.GONE);
                                    }
                                }).display();
                    }
                });
                inputEndTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new SingleDateAndTimePickerDialog.Builder(getContext())
                                .minutesStep(1)
                                .displayYears(true)
                                .bottomSheet()
                                .displayListener(new SingleDateAndTimePickerDialog.DisplayListener() {
                                    @Override
                                    public void onDisplayed(SingleDateAndTimePicker picker) {
                                        maskBackground.setVisibility(View.VISIBLE);
                                        bottomSheetDialog.hide();
                                    }
                                    @Override
                                    public void onClosed(SingleDateAndTimePicker picker) {
                                        bottomSheetDialog.show();
                                        maskBackground.setVisibility(View.GONE);
                                    }
                                })
                                .title(getString(R.string.end_time))
                                .listener(new SingleDateAndTimePickerDialog.Listener() {
                                    @Override
                                    public void onDateSelected(Date date) {
                                        SimpleDateFormat selectDate = new SimpleDateFormat("yyyyMMdd'T'HHmm00'Z'");
                                        selectDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                                        dtEnd[0] = selectDate.format(date);
                                        inputEndTime.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(date));
                                        bottomSheetDialog.show();
                                        maskBackground.setVisibility(View.GONE);
                                    }
                                }).display();
                    }
                });
                createButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (inputTitle.getText().toString().isEmpty() == true) {
                            new AlertDialog.Builder(getContext())
                                    .setTitle(getString(R.string.require_title))
                                    .setNegativeButton(android.R.string.ok, null)
                                    .show();
                        } else {
                            String content = "BEGIN:VEVENT"+
                                    "\nSUMMARY:"+ inputTitle.getText() +
                                    "\nDTSTART:"+dtStart[0] +
                                    "\nDTEND:"+dtEnd[0];

                            if (!inputLocation.getText().toString().isEmpty()) {
                                content+="\nLOCATION:"+inputLocation.getText();
                            }
                            if (!inputDescription.getText().toString().isEmpty()) {
                                content+="\nDESCRIPTION:"+inputDescription.getText();
                            }
                            content+="\nEND:VEVENT";
                            Intent intent = new Intent(getContext(), GeneratorResultActivity.class);
                            intent.putExtra("content", content);
                            startActivity(intent);
                            bottomSheetDialog.cancel();

                        }
                    }
                });
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.cancel();
                    }
                });
                bottomSheetDialog.show();
            }
        });
        CardView cardViewCreateWiFi = view.findViewById(R.id.create_wifi);
        cardViewCreateWiFi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.create_wifi_bottom_sheet_dialog_layout);
                bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                Button createButton = bottomSheetDialog.findViewById(R.id.create_button);
                Button closeButton = bottomSheetDialog.findViewById(R.id.cancel_button);
                TextInputEditText inputSSID = bottomSheetDialog.findViewById(R.id.input_ssid);
                TextInputEditText inputPassword = bottomSheetDialog.findViewById(R.id.input_password);
                TextInputLayout inputLayoutPassword = bottomSheetDialog.findViewById(R.id.inputlayout_password);
                CheckBox ssidHiddenCheckBox = bottomSheetDialog.findViewById(R.id.ssidHiddenCheckBox);
                encryptionType=0;
                AutoCompleteTextView encryption_spinner = bottomSheetDialog.findViewById(R.id.encryption_spinner);
                ArrayAdapter<CharSequence> adapter =
                        ArrayAdapter.createFromResource(getContext(),
                                R.array.encryption_array,
                                R.layout.list_item);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                encryption_spinner.setText(adapter.getItem(0).toString(), false);
                encryption_spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Long l = id;
                        encryptionType = l.intValue();
                        if (id==2) {
                            inputLayoutPassword.setVisibility(View.GONE);
                        } else {
                            inputLayoutPassword.setVisibility(View.VISIBLE);
                        }
                    }
                });
                encryption_spinner.setAdapter(adapter);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.cancel();
                    }
                });
                createButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (inputSSID.getText().toString().isEmpty()==true){
                            new AlertDialog.Builder(getContext())
                                    .setTitle(getString(R.string.require_ssid))
                                    .setNegativeButton(android.R.string.ok, null)
                                    .show();
                        } else {
                            if (encryptionType!=2&&inputPassword.getText().toString().isEmpty()){
                                new AlertDialog.Builder(getContext())
                                        .setTitle(getString(R.string.require_password))
                                        .setNegativeButton(android.R.string.ok, null)
                                        .show();
                            } else {
                                String content = "WIFI:S:";
                                content += inputSSID.getText().toString();
                                content += ";";
                                if (encryptionType==0){
                                    content += "P:";
                                    content += inputPassword.getText().toString();
                                    content += ";T:WPA;";
                                } else if (encryptionType==1){
                                    content += "P:";
                                    content += inputPassword.getText().toString();
                                    content += ";T:WEP;";
                                } else {
                                    content += "T:nopass;";
                                }
                                if (ssidHiddenCheckBox.isChecked()) {
                                    content += "H:true;;";
                                } else {
                                    content += ";";
                                }
                                Intent intent = new Intent(getContext(), GeneratorResultActivity.class);
                                intent.putExtra("content", content);
                                startActivity(intent);
                                bottomSheetDialog.cancel();
                            }
                        }
                    }
                });
                bottomSheetDialog.show();
            }
        });
        CardView cardViewCreateApplication = view.findViewById(R.id.create_application);
        cardViewCreateApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog loadingAppsProgressDialog = new ProgressDialog(getContext());
                loadingAppsProgressDialog.setMessage(getString(R.string.loading_apps));
                loadingAppsProgressDialog.setCancelable(false);
                loadingAppsProgressDialog.show();
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.create_application_bottom_sheet_dialog_layout);
                bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetListView  userInstalledApps = bottomSheetDialog.findViewById(R.id.installed_app_list);
                List<AppList> installedApps = getInstalledApps();
                AppAdapter installedAppAdapter = new AppAdapter(getContext(), installedApps);
                userInstalledApps.setAdapter(installedAppAdapter);
                userInstalledApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                     @Override
                     public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                         Intent intent = new Intent(getContext(), GeneratorResultActivity.class);
                         intent.putExtra("content", "https://play.google.com/store/apps/details?id="+installedApps.get(i).packages);
                         startActivity(intent);
                         bottomSheetDialog.cancel();
                     }
                 }
                );
                bottomSheetDialog.show();
                loadingAppsProgressDialog.cancel();
            }
        });

    }
    private List<AppList> getInstalledApps() {
        PackageManager pm = getContext().getPackageManager();
        List<AppList> apps = new ArrayList<AppList>();
        List<PackageInfo> packs = getContext().getPackageManager().getInstalledPackages(0);
        Collections.sort(packs, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo arg0, PackageInfo arg1) {
                CharSequence name0 = arg0.applicationInfo.loadLabel(getContext().getPackageManager());
                CharSequence name1 = arg1.applicationInfo.loadLabel(getContext().getPackageManager());
                if (name0 == null && name1 == null) {
                    return 0;
                }
                if (name0 == null) {
                    return -1;
                }
                if (name1 == null) {
                    return 1;
                }
                return name0.toString().compareTo(name1.toString());
            }
        });
        //List<PackageInfo> packs = getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            if (!isSystemPackage(p)) {
                String appName = p.applicationInfo.loadLabel(getContext().getPackageManager()).toString();
                Drawable icon = p.applicationInfo.loadIcon(getContext().getPackageManager());
                String packages = p.applicationInfo.packageName;
                apps.add(new AppList(appName, icon, packages));
            }
        }
        return apps;
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        if((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)!=0)
            return false;
        else if((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)!=0)
            return true;
        else return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED) == 0;
    }

    public class AppAdapter extends BaseAdapter {

        public LayoutInflater layoutInflater;
        public List<AppList> listStorage;

        public AppAdapter(Context context, List<AppList> customizedListView) {
            layoutInflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            listStorage = customizedListView;
        }

        @Override
        public int getCount() {
            return listStorage.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder listViewHolder;
            if(convertView == null){
                listViewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.installed_app_list, parent, false);

                listViewHolder.textInListView = convertView.findViewById(R.id.list_app_name);
                listViewHolder.imageInListView = convertView.findViewById(R.id.app_icon);
                listViewHolder.packageInListView= convertView.findViewById(R.id.app_package);
                convertView.setTag(listViewHolder);
            }else{
                listViewHolder = (ViewHolder)convertView.getTag();
            }
            listViewHolder.textInListView.setText(listStorage.get(position).getName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                listViewHolder.imageInListView.setImageTintList(null);
            }
            listViewHolder.imageInListView.setImageDrawable(listStorage.get(position).getIcon());
            listViewHolder.packageInListView.setText(listStorage.get(position).getPackages());

            return convertView;
        }

        class ViewHolder{
            TextView textInListView;
            ImageView imageInListView;
            TextView packageInListView;
        }
    }

    public class AppList {
        private final String name;
        Drawable icon;
        private final String packages;
        public AppList(String name, Drawable icon, String packages) {
            this.name = name;
            this.icon = icon;
            this.packages = packages;
        }
        public String getName() {
            return name;
        }
        public Drawable getIcon() {
            return icon;
        }
        public String getPackages() {
            return packages;
        }

    }
}
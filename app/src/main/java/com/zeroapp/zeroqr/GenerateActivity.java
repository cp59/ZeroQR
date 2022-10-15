package com.zeroapp.zeroqr;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class GenerateActivity extends AppCompatActivity {
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private String ZeroAppHostURL = "https://zeroapp.tk";
    private String QuickURLHostURL = ZeroAppHostURL+"/qurl";
    private int contentType;
    private ArrayList<TextInputLayout> inputs = new ArrayList<>();
    private ArrayList otherTypeInputs = new ArrayList();
    private ArrayList<Boolean> inputRequiredLists = new ArrayList<>();
    private ScrollView generateView;
    private View generateLayout;
    private String dtStart,dtEnd;
    private Date dtStartForPicker,dtEndForPicker;
    private TextWatcher wifiPasswordRequiredInputTextWatcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        wifiPasswordRequiredInputTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    inputs.get(1).setErrorEnabled(true);
                    inputs.get(1).setError(getString(R.string.field_required));
                } else {
                    inputs.get(1).setError(null);
                    inputs.get(1).setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        settings = getSharedPreferences("ZeroQR", 0);
        editor = settings.edit();
        contentType = getIntent().getIntExtra("CONTENT_TYPE", 0);
        generateView = findViewById(R.id.generateView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (contentType == ContentDecoder.ContentType.PLAIN_TEXT) {
            generateLayout = setView(R.string.generate_plain_text_type,R.layout.create_plain_text_layout);
            getTextInput(R.id.contentInputLayout,true);
        } else if (contentType == ContentDecoder.ContentType.URL) {
            generateLayout = setView(R.string.generate_url_type,R.layout.create_url_layout);
            TextInputLayout urlInputLayout = getTextInput(R.id.urlInputLayout,true);
            Button shortenUrlBtn = findViewById(R.id.shortenUrlBtn);
            shortenUrlBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder shortenURLAlertDialogBuilder = new AlertDialog.Builder(GenerateActivity.this);
                    View shortenURLLayout = getLayoutInflater().inflate(R.layout.shorten_url_dialog_layout, null);
                    shortenURLAlertDialogBuilder.setView(shortenURLLayout);
                    TextInputLayout customIDInputLayout = shortenURLLayout.findViewById(R.id.inputlayout_custom_id);
                    TextInputEditText inputURL = shortenURLLayout.findViewById(R.id.input_url);
                    TextInputEditText inputCustomID = shortenURLLayout.findViewById(R.id.input_custom_id);
                    inputURL.setText(getInputText(0));
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
                                new AlertDialog.Builder(GenerateActivity.this)
                                        .setTitle(getString(R.string.require_url))
                                        .setNegativeButton(android.R.string.ok, null)
                                        .show();
                            } else {
                                if (customIDCheckbox.isChecked() && inputCustomID.getText().toString().isEmpty()) {

                                    new AlertDialog.Builder(GenerateActivity.this)
                                            .setTitle(getString(R.string.require_custom_id))
                                            .setNegativeButton(android.R.string.ok, null)
                                            .show();
                                } else {
                                    if (!URLUtil.isValidUrl(inputURL.getText().toString())) {
                                        new AlertDialog.Builder(GenerateActivity.this)
                                                .setTitle(getString(R.string.invalid_url))
                                                .setNegativeButton(android.R.string.ok, null)
                                                .show();
                                    } else {

                                        ProgressDialog shortenURLProgressDialog = new ProgressDialog(GenerateActivity.this);
                                        shortenURLProgressDialog.setMessage(getString(R.string.shortening_url));
                                        shortenURLProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                        shortenURLProgressDialog.setCancelable(false);
                                        shortenURLProgressDialog.show();
                                        RequestQueue queue = Volley.newRequestQueue(GenerateActivity.this);
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
                                                                urlInputLayout.getEditText().setText(shortURLResp.getString("surl"));
                                                                shortenURLAlertDialog.cancel();
                                                                shortenURLProgressDialog.dismiss();
                                                            } else if (status.equals("error") && shortURLResp.getInt("errorID") == 1) {
                                                                shortenURLProgressDialog.dismiss();
                                                                new AlertDialog.Builder(GenerateActivity.this)
                                                                        .setTitle(getString(R.string.custom_id_already_used_dialog_title))
                                                                        .setMessage(getString(R.string.custom_id_already_used_dialog_message))
                                                                        .setNegativeButton(android.R.string.ok, null)
                                                                        .show();
                                                            } else {
                                                                shortenURLProgressDialog.dismiss();
                                                                new AlertDialog.Builder(GenerateActivity.this)
                                                                        .setTitle(getString(R.string.unable_to_shorten_url_dialog_title))
                                                                        .setMessage(getString(R.string.unable_to_shorten_url_dialog_message))
                                                                        .setNegativeButton(android.R.string.ok, null)
                                                                        .show();
                                                            }

                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                            shortenURLProgressDialog.dismiss();
                                                            new AlertDialog.Builder(GenerateActivity.this)
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
                                                new AlertDialog.Builder(GenerateActivity.this)
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
                    if (settings.getBoolean("firstTimeShortenURL", true)) {
                        new AlertDialog.Builder(GenerateActivity.this)
                                .setTitle(getString(R.string.first_time_shorten_url_dialog_title))
                                .setMessage(getString(R.string.first_time_shorten_url_dialog_message))
                                .setPositiveButton(getString(android.R.string.ok), null)
                                .show();
                        editor.putBoolean("firstTimeShortenURL", false);
                        editor.apply();
                    }
                }
            });
        } else if (contentType == ContentDecoder.ContentType.EMAIL) {
            generateLayout = setView(R.string.generate_email_type,R.layout.create_email_layout);
            getTextInput(R.id.addressInputLayout,false);
            getTextInput(R.id.subjectInputLayout,false);
            getTextInput(R.id.bodyInputLayout,false);
        } else if (contentType == ContentDecoder.ContentType.PHONE_NUMBER) {
            generateLayout = setView(R.string.generate_telephone_numbers_type,R.layout.create_telephone_layout);
            getTextInput(R.id.telephoneNumbersInputLayout,true);
        } else if (contentType == ContentDecoder.ContentType.SMS) {
            generateLayout = setView(R.string.generate_sms_type,R.layout.create_sms_layout);
            getTextInput(R.id.numberInputLayout,false);
            getTextInput(R.id.messageInputLayout,false);
        } else if (contentType == ContentDecoder.ContentType.GEOLOCATION) {
            generateLayout = setView(R.string.generate_location_type,R.layout.create_location_layout);
            getTextInput(R.id.latitudeInputLayout,true);
            getTextInput(R.id.longitudeInputLayout,true);
            Button useCurrentLocationBtn = findViewById(R.id.useCurrentLocationBtn);
            useCurrentLocationBtn.setOnClickListener(view -> startActivityForResult(new Intent(GenerateActivity.this,FindGeoLocationActivity.class),1));
        } else if (contentType == ContentDecoder.ContentType.CALENDAR_EVENT) {
            generateLayout = setView(R.string.generate_calendar_type,R.layout.create_calendar_layout);
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat selectDate = new SimpleDateFormat("yyyyMMdd'T'HHmm00'Z'");
            selectDate.setTimeZone(TimeZone.getTimeZone("GMT"));
            dtStart=selectDate.format(currentDate);
            dtStartForPicker = currentDate;
            dtEnd=selectDate.format(currentDate);
            dtEndForPicker = currentDate;
            getTextInput(R.id.titleInputLayout,true);
            TextInputLayout startTimeInputLayout = getTextInput(R.id.startTimeInputLayout,false);
            startTimeInputLayout.getEditText().setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(currentDate));
            startTimeInputLayout.getEditText().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DateTimePicker(GenerateActivity.this, getString(R.string.start_time),dtStartForPicker , new DateTimePicker.Listener() {
                        @Override
                        public void onDateSelected(Date date) {
                            SimpleDateFormat selectDate = new SimpleDateFormat("yyyyMMdd'T'HHmm00'Z'");
                            selectDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                            dtStart = selectDate.format(date);
                            dtStartForPicker=date;
                            startTimeInputLayout.getEditText().setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(date));
                        }
                    });
                }
            });
            TextInputLayout endTimeInputLayout = getTextInput(R.id.endTimeInputLayout,false);
            endTimeInputLayout.getEditText().setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(currentDate));
            endTimeInputLayout.getEditText().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DateTimePicker(GenerateActivity.this, getString(R.string.end_time),dtEndForPicker , new DateTimePicker.Listener() {
                        @Override
                        public void onDateSelected(Date date) {
                            SimpleDateFormat selectDate = new SimpleDateFormat("yyyyMMdd'T'HHmm00'Z'");
                            selectDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                            dtEnd = selectDate.format(date);
                            dtEndForPicker=date;
                            endTimeInputLayout.getEditText().setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(date));
                        }
                    });
                }
            });
            getTextInput(R.id.locationInputLayout,false);
            getTextInput(R.id.descriptionInputLayout,false);
        } else if (contentType==ContentDecoder.ContentType.WIFI) {
            generateLayout = setView(R.string.generate_wifi_type,R.layout.create_wifi_type);
            getTextInput(R.id.ssidInputLayout,true);
            TextInputLayout passwordInputLayout = findViewById(R.id.passwordInputLayout);
            inputRequiredLists.add(true);
            inputs.add(passwordInputLayout);
            passwordInputLayout.getEditText().addTextChangedListener(wifiPasswordRequiredInputTextWatcher);
            otherTypeInputs.add(findViewById(R.id.hiddenSsidCheckBox));
            AutoCompleteTextView encryptionSpinner = findViewById(R.id.encryptionSpinner);
            otherTypeInputs.add(0);
            ArrayAdapter<CharSequence> adapter =
                    ArrayAdapter.createFromResource(this,
                            R.array.encryption_array,
                            R.layout.encryption_type_list_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            encryptionSpinner.setText(adapter.getItem(0).toString(), false);
            encryptionSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    otherTypeInputs.set(1,(int) id);
                    if (id==2) {
                        inputRequiredLists.set(1,false);
                        passwordInputLayout.getEditText().removeTextChangedListener(wifiPasswordRequiredInputTextWatcher);
                        inputRequiredLists.set(1,false);
                        passwordInputLayout.setVisibility(View.GONE);
                    } else {
                        inputRequiredLists.set(1,true);
                        passwordInputLayout.getEditText().addTextChangedListener(wifiPasswordRequiredInputTextWatcher);
                        passwordInputLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
            encryptionSpinner.setAdapter(adapter);
        }

    }

    private TextInputLayout getTextInput(int inputId,Boolean fieldRequired) {
        TextInputLayout textInputLayout = generateLayout.findViewById(inputId);
        if (fieldRequired) {
            setFieldRequired(textInputLayout);
        } else {
            inputRequiredLists.add(false);
        }
        inputs.add(textInputLayout);
        return textInputLayout;
    }

    private View setView(int actionBarTitleStrId, int layoutId) {
        getSupportActionBar().setTitle(getString(actionBarTitleStrId));
        View generateLayout = getLayoutInflater().inflate(layoutId,null);
        generateLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        generateView.addView(generateLayout);
        return generateLayout;
    }

    private void setFieldRequired(TextInputLayout textInputLayout) {
        inputRequiredLists.add(true);
        textInputLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    if (textInputLayout.getEditText().getText().toString().isEmpty()) {
                        textInputLayout.setErrorEnabled(true);
                        textInputLayout.setError(getString(R.string.field_required));
                    } else {
                        textInputLayout.setError(null);
                        textInputLayout.setErrorEnabled(false);
                    }
                }
            }
        });
        textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(getString(R.string.field_required));
                } else {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void startGenerate(String content) {
        Intent intent = new Intent(GenerateActivity.this, GeneratorResultActivity.class);
        intent.putExtra("content", content);
        startActivity(intent);
        finish();
    }

    private boolean verifyField() {
        boolean allPass = true;
        for (int i = 0;i < inputRequiredLists.size();i++) {
            if (inputRequiredLists.get(i)) {
                if (inputs.get(i).getEditText().getText().toString().isEmpty()) {
                    inputs.get(i).setErrorEnabled(true);
                    inputs.get(i).setError(getString(R.string.field_required));
                    allPass=false;
                }
            }
        }
        return allPass;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.generate_activity_actionbar_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private String getInputText(int index) {
        return inputs.get(index).getEditText().getText().toString();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!verifyField()) {
            return super.onOptionsItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.actionGenerate:
                if (contentType==ContentDecoder.ContentType.PLAIN_TEXT) {
                    startGenerate(getInputText(0));
                } else if (contentType==ContentDecoder.ContentType.URL) {
                    startGenerate(getInputText(0));
                } else if (contentType==ContentDecoder.ContentType.EMAIL) {
                    String content = "mailto:"+getInputText(0)+"?";
                    if (!getInputText(1).isEmpty()) {
                        content += "subject="+getInputText(1)+"&";
                    }
                    if (!getInputText(2).isEmpty()) {
                        content += "body="+getInputText(2);
                    }
                    startGenerate(content);
                } else if (contentType==ContentDecoder.ContentType.PHONE_NUMBER) {
                    startGenerate("tel:"+getInputText(0));
                }  else if (contentType==ContentDecoder.ContentType.SMS) {
                    String content = "smsto:"+getInputText(0);
                    if (!getInputText(1).isEmpty()) {
                        content+=":"+getInputText(1);
                    }
                    startGenerate(content);
                } else if (contentType==ContentDecoder.ContentType.GEOLOCATION) {
                    startGenerate("geo:"+getInputText(0)+","+getInputText(1));
                } else if (contentType==ContentDecoder.ContentType.CALENDAR_EVENT) {
                    String content = "BEGIN:VEVENT"+
                            "\nSUMMARY:"+ getInputText(0) +
                            "\nDTSTART:"+dtStart +
                            "\nDTEND:"+dtEnd;

                    if (!getInputText(3).isEmpty()) {
                        content+="\nLOCATION:"+getInputText(3);
                    }
                    if (!getInputText(4).isEmpty()) {
                        content+="\nDESCRIPTION:"+getInputText(4);
                    }
                    content+="\nEND:VEVENT";
                    startGenerate(content);
                } else if (contentType==ContentDecoder.ContentType.WIFI) {
                    String content = "WIFI:S:";
                    int encryptionType = (int) otherTypeInputs.get(1);
                    content += getInputText(0);
                    content += ";";
                    if (encryptionType==0){
                        content += "P:";
                        content += getInputText(1);
                        content += ";T:WPA;";
                    } else if (encryptionType==1){
                        content += "P:";
                        content += getInputText(1);
                        content += ";T:WEP;";
                    } else {
                        content += "T:nopass;";
                    }
                    if (((CheckBox)otherTypeInputs.get(0)).isChecked()) {
                        content += "H:true;;";
                    } else {
                        content += ";";
                    }
                    startGenerate(content);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1) {
            if (data!=null) {
                inputs.get(0).getEditText().setText(data.getStringExtra("lat"));
                inputs.get(1).getEditText().setText(data.getStringExtra("long"));
            }
        }
    }
}
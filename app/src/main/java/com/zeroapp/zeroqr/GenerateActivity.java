package com.zeroapp.zeroqr;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class GenerateActivity extends AppCompatActivity {
    private int contentType;
    private final ArrayList<TextInputLayout> inputs = new ArrayList<>();
    private final ArrayList otherTypeInputs = new ArrayList();
    private final ArrayList<Boolean> inputRequiredLists = new ArrayList<>();
    private ScrollView generateView;
    private View generateLayout;
    private String dtStart,dtEnd;
    private Date dtStartForPicker,dtEndForPicker;
    private TextWatcher wifiPasswordRequiredInputTextWatcher;
    private MaterialToolbar topAppBar;
    private String pickContactDataType;
    private int fillInputIndex;
    private ActivityResultLauncher<Intent> launcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK)
            {
                Cursor cursor;
                Uri uri;
                if (result.getData() != null) {
                    uri = result.getData().getData ();
                    cursor = getContentResolver ().query (uri, null, null,null,null);
                    cursor.moveToFirst ();
                    inputs.get(fillInputIndex).getEditText().setText(cursor.getString(cursor.getColumnIndex (pickContactDataType)));
                    cursor.close();
                }
            }

        });
        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(view -> onBackPressed());
        topAppBar.setOnMenuItemClickListener(item -> {
            if (!verifyField()) {
                return false;
            }
            if (item.getItemId() == R.id.actionGenerate) {
                if (contentType == ContentDecoder.ContentType.PLAIN_TEXT) {
                    startGenerate(getInputText(0));
                } else if (contentType == ContentDecoder.ContentType.URL) {
                    startGenerate(getInputText(0));
                } else if (contentType == ContentDecoder.ContentType.EMAIL) {
                    String content = "mailto:" + getInputText(0) + "?";
                    if (!getInputText(1).isEmpty()) {
                        content += "subject=" + getInputText(1) + "&";
                    }
                    if (!getInputText(2).isEmpty()) {
                        content += "body=" + getInputText(2);
                    }
                    startGenerate(content);
                } else if (contentType == ContentDecoder.ContentType.PHONE_NUMBER) {
                    startGenerate("tel:" + getInputText(0));
                } else if (contentType == ContentDecoder.ContentType.SMS) {
                    String content = "smsto:" + getInputText(0);
                    if (!getInputText(1).isEmpty()) {
                        content += ":" + getInputText(1);
                    }
                    startGenerate(content);
                } else if (contentType == ContentDecoder.ContentType.GEOLOCATION) {
                    startGenerate("geo:" + getInputText(0) + "," + getInputText(1));
                } else if (contentType == ContentDecoder.ContentType.CALENDAR_EVENT) {
                    String content = "BEGIN:VEVENT" +
                            "\nSUMMARY:" + getInputText(0) +
                            "\nDTSTART:" + dtStart +
                            "\nDTEND:" + dtEnd;

                    if (!getInputText(3).isEmpty()) {
                        content += "\nLOCATION:" + getInputText(3);
                    }
                    if (!getInputText(4).isEmpty()) {
                        content += "\nDESCRIPTION:" + getInputText(4);
                    }
                    content += "\nEND:VEVENT";
                    startGenerate(content);
                } else if (contentType == ContentDecoder.ContentType.WIFI) {
                    String content = "WIFI:S:";
                    int encryptionType = (int) otherTypeInputs.get(1);
                    content += getInputText(0);
                    content += ";";
                    if (encryptionType == 0) {
                        content += "P:";
                        content += getInputText(1);
                        content += ";T:WPA;";
                    } else if (encryptionType == 1) {
                        content += "P:";
                        content += getInputText(1);
                        content += ";T:WEP;";
                    } else {
                        content += "T:nopass;";
                    }
                    if (((CheckBox) otherTypeInputs.get(0)).isChecked()) {
                        content += "H:true;;";
                    } else {
                        content += ";";
                    }
                    startGenerate(content);
                }
            }
            return false;
        });
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
        contentType = getIntent().getIntExtra("CONTENT_TYPE", 0);
        generateView = findViewById(R.id.generateView);
        if (contentType == ContentDecoder.ContentType.PLAIN_TEXT) {
            generateLayout = setView(R.string.generate_plain_text_type,R.layout.create_plain_text_layout);
            getTextInput(R.id.contentInputLayout,true);
        } else if (contentType == ContentDecoder.ContentType.URL) {
            generateLayout = setView(R.string.generate_url_type,R.layout.create_url_layout);
            getTextInput(R.id.urlInputLayout,true);
        } else if (contentType == ContentDecoder.ContentType.EMAIL) {
            generateLayout = setView(R.string.generate_email_type,R.layout.create_email_layout);
            getTextInput(R.id.addressInputLayout,false);
            getTextInput(R.id.subjectInputLayout,false);
            getTextInput(R.id.bodyInputLayout,false);
            Button fillInFromContactsBtn = findViewById(R.id.fillInFromContactsBtn);
            fillInFromContactsBtn.setOnClickListener(view -> fillInFromContacts(ContactsContract.CommonDataKinds.Email.ADDRESS,ContactsContract.CommonDataKinds.Email.CONTENT_URI,0));
        } else if (contentType == ContentDecoder.ContentType.PHONE_NUMBER) {
            generateLayout = setView(R.string.generate_telephone_numbers_type,R.layout.create_telephone_layout);
            getTextInput(R.id.telephoneNumbersInputLayout,true);
            Button fillInFromContactsBtn = findViewById(R.id.fillInFromContactsBtn);
            fillInFromContactsBtn.setOnClickListener(view -> fillInFromContacts(ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.CONTENT_URI,0));
        } else if (contentType == ContentDecoder.ContentType.SMS) {
            generateLayout = setView(R.string.generate_sms_type,R.layout.create_sms_layout);
            getTextInput(R.id.numberInputLayout,false);
            getTextInput(R.id.messageInputLayout,false);
            Button fillInFromContactsBtn = findViewById(R.id.fillInFromContactsBtn);
            fillInFromContactsBtn.setOnClickListener(view -> fillInFromContacts(ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.CONTENT_URI,0));
        } else if (contentType == ContentDecoder.ContentType.GEOLOCATION) {
            generateLayout = setView(R.string.generate_location_type,R.layout.create_location_layout);
            getTextInput(R.id.latitudeInputLayout,true);
            getTextInput(R.id.longitudeInputLayout,true);
            Button useCurrentLocationBtn = findViewById(R.id.useCurrentLocationBtn);
            ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK)
                {
                    Intent data = result.getData();
                    if (data!=null) {
                        inputs.get(0).getEditText().setText(data.getStringExtra("lat"));
                        inputs.get(1).getEditText().setText(data.getStringExtra("long"));
                    }
                }

            });
            useCurrentLocationBtn.setOnClickListener(view -> launcher.launch(new Intent(this,FindGeoLocationActivity.class)));
        } else if (contentType == ContentDecoder.ContentType.CALENDAR_EVENT) {
            generateLayout = setView(R.string.generate_calendar_type,R.layout.create_calendar_layout);
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat selectDate = new SimpleDateFormat("yyyyMMdd'T'HHmm00'Z'", Locale.getDefault());
            selectDate.setTimeZone(TimeZone.getTimeZone("GMT"));
            dtStart=selectDate.format(currentDate);
            dtStartForPicker = currentDate;
            dtEnd=selectDate.format(currentDate);
            dtEndForPicker = currentDate;
            getTextInput(R.id.titleInputLayout,true);
            TextInputLayout startTimeInputLayout = getTextInput(R.id.startTimeInputLayout,false);
            startTimeInputLayout.getEditText().setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault()).format(currentDate));
            startTimeInputLayout.getEditText().setOnClickListener(view -> new DateTimePicker(GenerateActivity.this, getString(R.string.start_time),dtStartForPicker , date -> {
                SimpleDateFormat selectDate12 = new SimpleDateFormat("yyyyMMdd'T'HHmm00'Z'",Locale.getDefault());
                selectDate12.setTimeZone(TimeZone.getTimeZone("GMT"));
                dtStart = selectDate12.format(date);
                dtStartForPicker=date;
                startTimeInputLayout.getEditText().setText(new SimpleDateFormat("yyyy/MM/dd HH:mm",Locale.getDefault()).format(date));
            }));
            TextInputLayout endTimeInputLayout = getTextInput(R.id.endTimeInputLayout,false);
            endTimeInputLayout.getEditText().setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault()).format(currentDate));
            endTimeInputLayout.getEditText().setOnClickListener(view -> new DateTimePicker(GenerateActivity.this, getString(R.string.end_time),dtEndForPicker , date -> {
                SimpleDateFormat selectDate1 = new SimpleDateFormat("yyyyMMdd'T'HHmm00'Z'",Locale.getDefault());
                selectDate1.setTimeZone(TimeZone.getTimeZone("GMT"));
                dtEnd = selectDate1.format(date);
                dtEndForPicker=date;
                endTimeInputLayout.getEditText().setText(new SimpleDateFormat("yyyy/MM/dd HH:mm",Locale.getDefault()).format(date));
            }));
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
            encryptionSpinner.setOnItemClickListener((parent, view, position, id) -> {
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
            });
            encryptionSpinner.setAdapter(adapter);
        }

    }

    private void fillInFromContacts(String dataTypeLocal, Uri dataTypeContentUri, int fillInputIndexLocal) {
        fillInputIndex = fillInputIndexLocal;
        pickContactDataType = dataTypeLocal;

        launcher.launch(new Intent(Intent.ACTION_PICK, dataTypeContentUri));
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
        topAppBar.setTitle(getString(actionBarTitleStrId));
        View generateLayout = getLayoutInflater().inflate(layoutId,null);
        generateLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        generateView.addView(generateLayout);
        return generateLayout;
    }

    private void setFieldRequired(TextInputLayout textInputLayout) {
        inputRequiredLists.add(true);
        textInputLayout.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                if (textInputLayout.getEditText().getText().toString().isEmpty()) {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(getString(R.string.field_required));
                } else {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
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

    private String getInputText(int index) {
        return inputs.get(index).getEditText().getText().toString();
    }

}
package com.zeroapp.zeroqr;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class ResultActivity extends AppCompatActivity {
    private String content,parsedContent;
    private String mailAddress, mailSubject, mailBody, number, smsMessage, networkSSID, networkEncryption, networkPass;
    private Float latitude;
    private Float longitude;
    private List<Map<String, String>> listArray;
    private static final String DataBaseName = "HistoryDataBase";
    private static final int DataBaseVersion = 1;
    private ContentValues contentValues;
    private Intent eventIntent;
    public void addResultListData(String titleKey, String detailKey) {
        Map<String, String> listItem = new HashMap<>();
        listItem.put("titleKey", titleKey);
        listItem.put("detailKey", detailKey);
        if (titleKey!=getString(R.string.type)){
            parsedContent+=titleKey+":"+detailKey+"\n";
        } else {
            contentValues.put("type", detailKey);
        }
        listArray.add(listItem);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        setTitle(getString(R.string.qr_code_detected));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences settings = getApplicationContext().getSharedPreferences("ZeroQR", 0);
        String dataBaseTable = "History";
        HistorySQLDataBaseHelper historySQLDataBaseHelper = new HistorySQLDataBaseHelper(getApplicationContext(), DataBaseName, null, DataBaseVersion, dataBaseTable);
        SQLiteDatabase db = historySQLDataBaseHelper.getWritableDatabase();
        content = getIntent().getStringExtra("CONTENT");
        parsedContent="";
        ListView resultListView = findViewById(R.id.resultListView);
        View resultActionLayout = getLayoutInflater().inflate(R.layout.result_action_layout,null,false);
        resultListView.addFooterView(resultActionLayout);
        CardView shareButton = resultActionLayout.findViewById(R.id.shareResultButton);
        CardView copyButton = resultActionLayout.findViewById(R.id.copyResultButton);
        CardView sendEmailButton = resultActionLayout.findViewById(R.id.sendEmailButton);
        CardView sendMessageButton = resultActionLayout.findViewById(R.id.sendMessageButton);
        CardView openPhoneButton = resultActionLayout.findViewById(R.id.openPhoneButton);
        CardView openMapsButton = resultActionLayout.findViewById(R.id.openMapsButton);
        CardView connectNetworkButton = resultActionLayout.findViewById(R.id.connectNetworkButton);
        CardView openBrowserButton = resultActionLayout.findViewById(R.id.openBrowserButton);
        CardView addCalendarButton = resultActionLayout.findViewById(R.id.addCalendarButton);
        CardView searchBookButton = resultActionLayout.findViewById(R.id.searchBookButton);
        CardView viewQRCodeButton = resultActionLayout.findViewById(R.id.viewQRCodeButton);
        listArray = new ArrayList<>();
        contentValues = new ContentValues();
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy'/'MM'/'dd HH':'mm");
        Date date = new Date(System.currentTimeMillis());
        contentValues.put("time",formatter.format(date));
        contentValues.put("source",getString(R.string.scan_mode));
        Uri contentUri = Uri.parse(content);
        String contentScheme = contentUri.getScheme();
        if (contentScheme==null){
            contentScheme="";
        }
        try {
            if (contentScheme.equalsIgnoreCase("smsto")) {
                number = content.split(":")[1];
                smsMessage = content.substring(7 + content.split(":")[1].length());
                addResultListData(getString(R.string.type), getString(R.string.sms));
                addResultListData(getString(R.string.number), number);
                addResultListData(getString(R.string.message), smsMessage);
                sendMessageButton.setVisibility(View.VISIBLE);
                if (!number.isEmpty()) {
                    openPhoneButton.setVisibility(View.VISIBLE);
                }
            } else if (contentScheme.equalsIgnoreCase("http") || contentScheme.equalsIgnoreCase("https")) {
                addResultListData(getString(R.string.type), getString(R.string.url));
                addResultListData(getString(R.string.url), content);
                openBrowserButton.setVisibility(View.VISIBLE);
            } else if (contentScheme.equalsIgnoreCase("tel")) {
                number = content.substring(4);
                addResultListData(getString(R.string.type), getString(R.string.telephone_numbers));
                addResultListData(getString(R.string.telephone_numbers), number);
                openPhoneButton.setVisibility(View.VISIBLE);
            } else if (contentScheme.equalsIgnoreCase("wifi")) {
                try {
                    networkSSID = content.split("S:")[1].split(";")[0];
                } catch (Exception e) {
                    networkSSID = "";
                }
                if (networkSSID != "") {
                    try {
                        networkPass = content.split("P:")[1].split(";")[0];
                    } catch (Exception e) {
                        networkPass = getString(R.string.no_password);
                    }
                    networkEncryption = content.split("T:")[1].split(";")[0];
                    if (networkEncryption.equalsIgnoreCase("nopass")) {
                        networkEncryption = getString(R.string.none);
                    }
                    addResultListData(getString(R.string.type), getString(R.string.wifi));
                    addResultListData(getString(R.string.ssid), networkSSID);
                    addResultListData(getString(R.string.password), networkPass);
                    addResultListData(getString(R.string.encryption), networkEncryption);
                } else {
                    addResultListData(getString(R.string.type), getString(R.string.plain_text));
                    addResultListData(getString(R.string.content), content);
                }
                connectNetworkButton.setVisibility(View.VISIBLE);
            } else if (contentScheme.equalsIgnoreCase("mailto")) {
                mailAddress = content.substring("mailto".length() + 1).split(Pattern.quote("?"))[0];
                mailSubject = content.split("subject=")[1].split("&")[0];
                mailBody = content.split("body=")[1].split("&")[0];
                addResultListData(getString(R.string.type), getString(R.string.email));
                addResultListData(getString(R.string.address), mailAddress);
                addResultListData(getString(R.string.subject), mailSubject);
                addResultListData(getString(R.string.body), mailBody);
                sendEmailButton.setVisibility(View.VISIBLE);
            } else if (contentScheme.equalsIgnoreCase("geo")) {
                latitude = Float.valueOf(content.substring("geo:".length()).split(",")[0]);
                longitude = Float.valueOf(content.substring("geo:".length()).split(",")[1]);
                addResultListData(getString(R.string.type), getString(R.string.location));
                addResultListData(getString(R.string.latitude), String.valueOf(latitude));
                addResultListData(getString(R.string.longitude), String.valueOf(longitude));
                openMapsButton.setVisibility(View.VISIBLE);
            } else if (content.toLowerCase().startsWith("begin:vcard")) {
                addResultListData(getString(R.string.type), getString(R.string.contact));
                addResultListData(getString(R.string.content), content);
            } else if (content.toUpperCase().startsWith("BEGIN:VEVENT")) {
                String[] contentLineList = content.split("\n");
                String summary = null;
                String location = null;
                String description = null;
                String dtStart = null;
                Date dtStartDate = null;
                Date dtEndDate = null;
                String dtEnd = null;
                for (int i = 0; i < contentLineList.length; i++) {
                    String contentLine = contentLineList[i];
                    if (contentLine.toUpperCase().startsWith("SUMMARY:")) {
                        summary = contentLine.substring(8);
                    } else if (contentLine.toUpperCase().startsWith("DTSTART:")) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        dtStartDate = simpleDateFormat.parse(contentLine.substring(8));
                        dtStart = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(dtStartDate);
                    } else if (contentLine.toUpperCase().startsWith("DTEND:")) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        dtEndDate = simpleDateFormat.parse(contentLine.substring(6));
                        dtEnd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(dtEndDate);
                    } else if (contentLine.toUpperCase().startsWith("LOCATION:")) {
                        location = contentLine.substring(9);
                    } else if (contentLine.toUpperCase().startsWith("DESCRIPTION:")) {
                        description = contentLine.substring(12);
                    }
                }
                addResultListData(getString(R.string.type), getString(R.string.calendar));
                addResultListData(getString(R.string.title), summary);
                addResultListData(getString(R.string.start_time), dtStart);
                addResultListData(getString(R.string.end_time), dtEnd);
                eventIntent = new Intent(Intent.ACTION_INSERT);
                eventIntent.setType("vnd.android.cursor.item/event");
                eventIntent.putExtra("beginTime", dtStartDate.getTime());
                eventIntent.putExtra("endTime", dtEndDate.getTime());
                eventIntent.putExtra("title", summary);
                eventIntent.putExtra("eventLocation", location);
                eventIntent.putExtra("description", description);
                if (location != null) {
                    addResultListData(getString(R.string.location), location);
                }
                if (description != null) {
                    addResultListData(getString(R.string.description), description);
                }
                addCalendarButton.setVisibility(View.VISIBLE);
            } else if (content.startsWith("978")||content.startsWith("979")){
                addResultListData(getString(R.string.type),getString(R.string.book));
                addResultListData("ISBN",content);
                searchBookButton.setVisibility(View.VISIBLE);

            } else {
                addResultListData(getString(R.string.type), getString(R.string.plain_text));
                addResultListData(getString(R.string.content), content);
            }
        } catch (Exception e) {
            e.printStackTrace();
            parsedContent="";
            listArray.clear();
            addResultListData(getString(R.string.type), getString(R.string.plain_text));
            addResultListData(getString(R.string.content), content);
        }
        final Boolean saveToHistory = getIntent().getBooleanExtra("saveToHistory", settings.getBoolean("saveScanHistory",true));
        contentValues.put("content",content);
        parsedContent = parsedContent.substring(0, parsedContent.lastIndexOf("\n"));
        contentValues.put("parsedContent",parsedContent);
        if (saveToHistory) {
            db.insert(dataBaseTable, null, contentValues);
            Integer maxHistorySaveNumber = settings.getInt("maxHistorySaveNumber",0);
            if (maxHistorySaveNumber!=0) {
                long historyNumber = DatabaseUtils.queryNumEntries(db,"History");
                if (historyNumber>maxHistorySaveNumber) {
                    db.execSQL("Delete from History where _id IN (Select _id from History limit 1);\n");
                }
            }
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(ResultActivity.this,listArray,R.layout.scan_result_list_item,new String[]{"titleKey","detailKey"},new int[]{R.id.text1,R.id.text2});
        resultListView.setAdapter(simpleAdapter);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareintent = new Intent(Intent.ACTION_SEND);
                shareintent.setType("text/plain");
                shareintent.putExtra(Intent.EXTRA_TEXT, content);
                startActivity(Intent.createChooser(shareintent, getString(R.string.share)));
            }
        });
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("text label", content);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ResultActivity.this, getString(R.string.copied), Toast.LENGTH_LONG).show();
            }
        });
        viewQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultActivity.this, GeneratorResultActivity.class);
                intent.putExtra("content", content);
                intent.putExtra("saveToHistory",false);
                startActivity(intent);
            }
        });
        openPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number)));
            }
        });
        openMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settings.getBoolean("openLinkInExternalBrowser",false)==true) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://maps.google.com/local?q=" + latitude + "," + longitude));
                    startActivity(i);
                } else {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(ResultActivity.this, Uri.parse("https://maps.google.com/local?q=" + latitude + "," + longitude));
                }
            }
        });
        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, mailAddress);
                intent.putExtra(Intent.EXTRA_SUBJECT, mailSubject);
                intent.putExtra(Intent.EXTRA_TEXT, mailBody);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                Uri uri = Uri.parse("smsto:" + number);
                intent.setData(uri);
                intent.putExtra("sms_body", smsMessage);
                startActivity(intent);
            }
        });
        connectNetworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Toast.makeText(ResultActivity.this,getString(R.string.wifi_connect_not_support_android_10),Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                } else {
                    Toast.makeText(ResultActivity.this,getString(R.string.connecting_to_wifi),Toast.LENGTH_LONG).show();
                    WifiConfiguration conf = new WifiConfiguration();
                    conf.SSID = "\"" + networkSSID + "\"";
                    if (networkEncryption.equalsIgnoreCase("WEP")) {
                        conf.wepKeys[0] = "\"" + networkPass + "\"";
                        conf.wepTxKeyIndex = 0;
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    } else if (networkEncryption.equalsIgnoreCase("WPA")) {
                        conf.preSharedKey = "\""+ networkPass +"\"";
                    } else {
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    }
                    WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager.addNetwork(conf);
                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                    for( WifiConfiguration i : list ) {
                        if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                            wifiManager.disconnect();
                            wifiManager.enableNetwork(i.networkId, true);
                            wifiManager.reconnect();
                            break;
                        }
                    }

                }

            }
        });
        addCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(eventIntent);
            }
        });
        openBrowserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (settings.getBoolean("openLinkInExternalBrowser",false)==true) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(content));
                        startActivity(i);
                    } else {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(ResultActivity.this, Uri.parse(content));
                    }
                } catch (Exception e) {
                    Toast.makeText(ResultActivity.this, getString(R.string.unable_to_load_url), Toast.LENGTH_LONG).show();
                }
            }
        });
        searchBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (settings.getBoolean("openLinkInExternalBrowser",false)==true) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("http://books.google.com/books?vid=ISBN"+content));
                    startActivity(i);
                } else {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(ResultActivity.this, Uri.parse("http://books.google.com/books?vid=ISBN"+content));
                }
            }
        });
        if (content.toLowerCase().startsWith("begin:vcard")) {
            try {
                File cachePath = new File(getCacheDir(), "files");
                cachePath.mkdirs();
                File newFile = new File(cachePath,"contact.vcf");
                FileOutputStream stream = new FileOutputStream(newFile);
                try {
                    stream.write(content.getBytes());
                } finally {
                    stream.close();
                }
                Uri contactUri = FileProvider.getUriForFile(ResultActivity.this, "com.zeroapp.zeroqr.fileprovider", newFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(contactUri,"text/vcard");
                startActivity(intent);
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getDefaultSmsAppPackageName(@NonNull Context context) {
        String defaultSmsPackageName;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context);
            return defaultSmsPackageName;
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_DEFAULT).setType("vnd.android-dir/mms-sms");
            final List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
            if (resolveInfos != null && !resolveInfos.isEmpty())
                return resolveInfos.get(0).activityInfo.packageName;

        }
        return null;
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
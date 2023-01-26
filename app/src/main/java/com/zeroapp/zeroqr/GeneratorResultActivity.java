package com.zeroapp.zeroqr;

import static android.os.ext.SdkExtensions.getExtensionVersion;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;


public class GeneratorResultActivity extends AppCompatActivity {
    private String content;
    private ImageView imageViewResult;
    private BarcodeEncoder barcodeEncoder;
    private Bitmap resultBitmap;
    private Bitmap resultBitmapWithTitle;
    public static Bitmap resultBitmapWithLogo;
    private Bitmap logoBitmap;
    private Uri logoUri;
    private Hashtable<EncodeHintType, java.io.Serializable> hints;
    private Integer selectedSize;
    private Integer color;
    private Integer bgColor;
    private Integer titleColor;
    private String title;
    private Boolean isChangeColor;
    private String parsedContent;
    private SharedPreferences settings;
    private static final String DataBaseName = "HistoryDataBase";
    private static final int DataBaseVersion = 1;
    private static final String DataBaseTable = "History";
    private static SQLiteDatabase db;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generator_result);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(view -> onBackPressed());
        HistorySQLDataBaseHelper historySQLDataBaseHelper = new HistorySQLDataBaseHelper(GeneratorResultActivity.this);
        db = historySQLDataBaseHelper.getWritableDatabase();
        settings = getApplicationContext().getSharedPreferences("ZeroQR", 0);
        selectedSize = 512;
        color = Color.BLACK;
        bgColor = Color.WHITE;
        titleColor = Color.BLACK;
        title = "";
        parsedContent = "";
        isChangeColor = false;
        content = getIntent().getStringExtra("content");
        if (getIntent().getBooleanExtra("saveToHistory", settings.getBoolean("saveCreateHistory", true))) {
            saveToHistory(content);
        }
        imageViewResult = findViewById(R.id.iv_result);
        imageViewResult.setImageTintList(null);
        TextView textViewResult = findViewById(R.id.tv_result);
        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        bottomAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_size:
                    List<String> sizeArrayList = new ArrayList<>();
                    sizeArrayList.add("100x100");
                    sizeArrayList.add("200x200");
                    sizeArrayList.add("300x300");
                    sizeArrayList.add("400x400");
                    sizeArrayList.add("500x500");
                    sizeArrayList.add("600x600");
                    sizeArrayList.add("700x700");
                    sizeArrayList.add("800x800");
                    sizeArrayList.add("900x900");
                    sizeArrayList.add("1000x1000");
                    final CharSequence[] sizeList = sizeArrayList.toArray(new String[0]);
                    new MaterialAlertDialogBuilder(GeneratorResultActivity.this)
                            .setTitle(getString(R.string.size))
                            .setItems(sizeList, (dialog, item1) -> {
                                selectedSize = Integer.valueOf(sizeList[item1].toString().split("x")[0]);
                                createQRCode();
                            })
                            .show();
                    return false;
                case R.id.action_color:
                    List<String> colorArrayList = new ArrayList<>();
                    colorArrayList.add(getString(R.string.foreground_color));
                    colorArrayList.add(getString(R.string.background_color));
                    colorArrayList.add(getString(R.string.title_color));
                    final CharSequence[] colorList = colorArrayList.toArray(new String[0]);
                    new MaterialAlertDialogBuilder(GeneratorResultActivity.this)
                            .setTitle(getString(R.string.color))
                            .setItems(colorList, (dialog, item12) -> {
                                if (item12 == 0) {
                                    new ColorPickerDialog.Builder(GeneratorResultActivity.this)
                                            .setTitle(getString(R.string.foreground_color))
                                            .setPreferenceName("QRCodeColorPicker")
                                            .setPositiveButton(getString(android.R.string.ok),
                                                    (ColorEnvelopeListener) (envelope, fromUser) -> {
                                                        color = envelope.getColor();
                                                        createQRCode();
                                                    })
                                            .setNegativeButton(getString(android.R.string.cancel),
                                                    (dialogInterface, i) -> dialogInterface.dismiss())
                                            .attachAlphaSlideBar(true) // the default value is true.
                                            .attachBrightnessSlideBar(true)  // the default value is true.
                                            .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                                            .show();
                                } else if (item12 == 1) {
                                    new ColorPickerDialog.Builder(GeneratorResultActivity.this)
                                            .setTitle(getString(R.string.background_color))
                                            .setPreferenceName("QRCodeBackgroundColorPicker")
                                            .setPositiveButton(getString(android.R.string.ok),
                                                    (ColorEnvelopeListener) (envelope, fromUser) -> {
                                                        bgColor = envelope.getColor();
                                                        createQRCode();
                                                    })
                                            .setNegativeButton(getString(android.R.string.cancel),
                                                    (dialogInterface, i) -> dialogInterface.dismiss())
                                            .attachAlphaSlideBar(true) // the default value is true.
                                            .attachBrightnessSlideBar(true)  // the default value is true.
                                            .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                                            .show();
                                } else {
                                    new ColorPickerDialog.Builder(GeneratorResultActivity.this)
                                            .setTitle(getString(R.string.title_color))
                                            .setPreferenceName("QRCodeTitleColorPicker")
                                            .setPositiveButton(getString(android.R.string.ok),
                                                    (ColorEnvelopeListener) (envelope, fromUser) -> {
                                                        titleColor = envelope.getColor();
                                                        createQRCode();
                                                    })
                                            .setNegativeButton(getString(android.R.string.cancel),
                                                    (dialogInterface, i) -> dialogInterface.dismiss())
                                            .attachAlphaSlideBar(true) // the default value is true.
                                            .attachBrightnessSlideBar(true)  // the default value is true.
                                            .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                                            .show();
                                }
                            })
                            .show();
                    return false;
                case R.id.action_logo:
                    if (isPhotoPickerAvailable()) {
                        pickMedia.launch(new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build());
                    } else {
                        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        getIntent.setType("image/*");
                        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        pickIntent.setType("image/*");
                        Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_image));
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                            if (result.getResultCode() == RESULT_OK)
                            {
                                Intent data = result.getData();
                                logoUri = data.getData();
                                try {
                                    logoBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), logoUri);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                createQRCode();
                            }
                        });
                        launcher.launch(chooserIntent);
                    }
                    return false;
                case R.id.action_title:
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(GeneratorResultActivity.this);
                    builder.setTitle(getString(R.string.title));
                    final View setQRCodeTitleDialogLayout = getLayoutInflater().inflate(R.layout.set_qrcode_title_dialog_layout, null);
                    builder.setView(setQRCodeTitleDialogLayout);
                    final TextInputEditText inputTitle = setQRCodeTitleDialogLayout.findViewById(R.id.input_title);
                    inputTitle.setText(title);
                    builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        title = inputTitle.getText().toString();
                        createQRCode();
                    });
                    builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

                    builder.show();
                    return false;
                default:
                    return false;
            }
        });
        FloatingActionButton shareResultFab = findViewById(R.id.shareResultFab);
        FloatingActionButton saveResultFab = findViewById(R.id.saveResultFab);
        shareResultFab.setOnClickListener(v -> {

            try {
                File cachePath = new File(getCacheDir(), "images");
                cachePath.mkdirs();
                FileOutputStream stream = new FileOutputStream(cachePath + "/shareImage.png");
                resultBitmapWithLogo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
                File newFile = new File(cachePath, "shareImage.png");
                Uri contentUri = FileProvider.getUriForFile(GeneratorResultActivity.this, "com.zeroapp.zeroqr.fileprovider", newFile);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)));

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        saveResultFab.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(GeneratorResultActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                saveImage(resultBitmapWithLogo, GeneratorResultActivity.this, "ZeroQR");
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.saved_to_album), Snackbar.LENGTH_LONG).show();

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(GeneratorResultActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                MainActivity.storagePermissionDenied(GeneratorResultActivity.this);

            } else {
                ActivityCompat.requestPermissions(GeneratorResultActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

            }
        });
        barcodeEncoder = new BarcodeEncoder();
        try {
            hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2);
            resultBitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE,
                    selectedSize, selectedSize, hints);
            textViewResult.setText(content);
            imageViewResult.setImageBitmap(resultBitmap);
            resultBitmapWithTitle = resultBitmap;
            resultBitmapWithLogo = resultBitmap;

        } catch (WriterException e) {
            bottomAppBar.setVisibility(View.INVISIBLE);
            //textViewResult.setText("無法建立\n原因:"+e.toString());
            textViewResult.setText(content);
            imageViewResult.setImageResource(R.drawable.ic_error);
            e.printStackTrace();
        }
        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        logoUri = uri;
                        try {
                            logoBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), logoUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        createQRCode();
                    }
                });
    }
    private boolean isPhotoPickerAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return getExtensionVersion(Build.VERSION_CODES.R) >= 2;
        } else
            return false;
    }
    private void setLogo(Bitmap logo) {
        int srcWidth = resultBitmapWithTitle.getWidth();
        int srcHeight = resultBitmapWithTitle.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        float scaleFactor = srcWidth * 1.0f / 7 / logoWidth;
        resultBitmapWithLogo = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(resultBitmapWithLogo);
            canvas.drawBitmap(resultBitmapWithTitle, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            resultBitmapWithLogo = null;
            e.getStackTrace();
        }
    }

    public void addParseData(String titleKey, String detailKey){
        if (!titleKey.equals(getString(R.string.type))){
            parsedContent+=titleKey+":"+detailKey+"\n";
        }
    }
    public void saveToHistory(String content){
        ContentValues contentValues = new ContentValues();
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy'/'MM'/'dd HH':'mm", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        contentValues.put("time",formatter.format(date));
        contentValues.put("source",getString(R.string.create_mode));
        Uri contentUri = Uri.parse(content);
        String contentScheme = contentUri.getScheme();
        try {
            if (contentScheme.equalsIgnoreCase("smsto")) {
                String number = content.split(":")[1];
                String smsMessage = content.substring(7 + content.split(":")[1].length());
                addParseData(getString(R.string.number), number);
                addParseData(getString(R.string.message), smsMessage);
                contentValues.put("type",getString(R.string.sms));
            } else if (contentScheme.equalsIgnoreCase("http") || contentScheme.equalsIgnoreCase("https")) {
                addParseData(getString(R.string.url), content);
                contentValues.put("type",getString(R.string.url));
            } else if (contentScheme.equalsIgnoreCase("tel")) {
                String number = content.substring(4);
                addParseData(getString(R.string.telephone_numbers), number);
                contentValues.put("type",getString(R.string.telephone_numbers));
            } else if (contentScheme.equalsIgnoreCase("wifi")) {
                String networkSSID;
                String networkPass;
                String networkEncryption;
                try {
                    networkSSID = content.split("S:")[1].split(";")[0];
                } catch (Exception e) {
                    networkSSID = "";
                }
                if (!networkSSID.isEmpty()) {
                    try {
                        networkPass = content.split("P:")[1].split(";")[0];
                    } catch (Exception e) {
                        networkPass = getString(R.string.no_password);
                    }
                    networkEncryption = content.split("T:")[1].split(";")[0];
                    if (networkEncryption.equalsIgnoreCase("nopass")) {
                        networkEncryption = getString(R.string.none);
                    }
                    addParseData(getString(R.string.ssid), networkSSID);
                    addParseData(getString(R.string.password), networkPass);
                    addParseData(getString(R.string.encryption), networkEncryption);
                } else {
                    addParseData(getString(R.string.content), content);
                }
                contentValues.put("type",getString(R.string.wifi));
            } else if (contentScheme.equalsIgnoreCase("mailto")) {
                String mailAddress = content.substring("mailto".length() + 1).split(Pattern.quote("?"))[0];
                String mailSubject = content.split("subject=")[1].split("&")[0];
                String mailBody = content.split("body=")[1].split("&")[0];
                addParseData(getString(R.string.address), mailAddress);
                addParseData(getString(R.string.subject), mailSubject);
                addParseData(getString(R.string.body), mailBody);
                contentValues.put("type",getString(R.string.email));
            } else if (contentScheme.equalsIgnoreCase("geo")) {
                Float latitude = Float.valueOf(content.substring("geo:".length()).split(",")[0]);
                Float longitude = Float.valueOf(content.substring("geo:".length()).split(",")[1]);
                addParseData(getString(R.string.latitude), String.valueOf(latitude));
                addParseData(getString(R.string.longitude), String.valueOf(longitude));
                contentValues.put("type",getString(R.string.location));
            } else if (content.toLowerCase().startsWith("begin:vcard")) {
                addParseData(getString(R.string.content), content);
                contentValues.put("type",getString(R.string.contact));
            } else if (content.toUpperCase().startsWith("BEGIN:VEVENT")) {
                String[] contentLineList = content.split("\n");
                String summary = null;
                String location = null;
                String description = null;
                String dtStart = null;
                Date dtStartDate;
                Date dtEndDate;
                String dtEnd = null;
                for (String contentLine : contentLineList) {
                    if (contentLine.toUpperCase().startsWith("SUMMARY:")) {
                        summary = contentLine.substring(8);
                    } else if (contentLine.toUpperCase().startsWith("DTSTART:")) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.getDefault());
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        dtStartDate = simpleDateFormat.parse(contentLine.substring(8));
                        dtStart = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(dtStartDate);
                    } else if (contentLine.toUpperCase().startsWith("DTEND:")) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.getDefault());
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        dtEndDate = simpleDateFormat.parse(contentLine.substring(6));
                        dtEnd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(dtEndDate);
                    } else if (contentLine.toUpperCase().startsWith("LOCATION:")) {
                        location = contentLine.substring(9);
                    } else if (contentLine.toUpperCase().startsWith("DESCRIPTION:")) {
                        description = contentLine.substring(12);
                    }
                }
                addParseData(getString(R.string.title), summary);
                addParseData(getString(R.string.start_time), dtStart);
                addParseData(getString(R.string.end_time), dtEnd);
                if (location != null) {
                    addParseData(getString(R.string.location), location);
                }
                if (description != null) {
                    addParseData(getString(R.string.description), description);
                }
                contentValues.put("type",getString(R.string.calendar));

            } else {
                addParseData(getString(R.string.content), content);
                contentValues.put("type",getString(R.string.plain_text));
            }
        } catch (Exception e) {
            e.printStackTrace();
            addParseData(getString(R.string.content), content);
            contentValues.put("type",getString(R.string.plain_text));
        }
        contentValues.put("content",content);
        parsedContent = parsedContent.substring(0, parsedContent.lastIndexOf("\n"));
        contentValues.put("parsedContent",parsedContent);
        db.insert(DataBaseTable, null, contentValues);
        int maxHistorySaveNumber = settings.getInt("maxHistorySaveNumber",0);
        if (maxHistorySaveNumber!=0) {
            long historyNumber = DatabaseUtils.queryNumEntries(db,"History");
            if (historyNumber>maxHistorySaveNumber) {
                db.execSQL("Delete from History where _id IN (Select _id from History limit 1);\n");
            }
        }
    }
    public void setQRCodeTitle(String string) {
        resultBitmapWithTitle = Bitmap.createBitmap(resultBitmap.getWidth(), resultBitmap.getHeight(), resultBitmap.getConfig());
        Canvas canvas = new Canvas(resultBitmapWithTitle);
        canvas.drawBitmap(resultBitmap, 0, 0, null);
        Paint paint = new Paint();
        paint.setColor(titleColor);
        paint.setTextSize(selectedSize/20);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(string, resultBitmap.getWidth()/2, resultBitmap.getHeight()-5, paint);
    }
    public void createQRCode(){
        try {
            BitMatrix matrix = barcodeEncoder.encode(content, BarcodeFormat.QR_CODE,
                    selectedSize, selectedSize, hints);
            int w = matrix.getWidth();
            int h = matrix.getHeight();
            int[] rawData = new int[w * h];
            for (int i = 0; i < w; i++){
                for (int j = 0; j < h; j++){
                    if (matrix.get(i, j)) {
                        rawData[i + (j * w)] = color;
                    } else {
                        rawData[i + (j * w)] = bgColor;
                    }
                }
            }


            resultBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            resultBitmap.setPixels(rawData, 0, w, 0, 0, w, h);
            imageViewResult.setImageBitmap(resultBitmap);
            if (!title.isEmpty()) {
                setQRCodeTitle(title);
            } else {
                resultBitmapWithTitle = resultBitmap;
            }
            if (logoUri!=null){
                setLogo(logoBitmap);
            } else {
                resultBitmapWithLogo = resultBitmapWithTitle;
            }
            imageViewResult.setImageBitmap(resultBitmapWithLogo);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (isChangeColor && color!=null) {
            isChangeColor=false;
            createQRCode();
        } else if (MainActivity.requestingStoragePermission) {
            MainActivity.requestingStoragePermission=false;
            if (ContextCompat.checkSelfPermission(GeneratorResultActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                saveImage(resultBitmapWithLogo, GeneratorResultActivity.this, "ZeroQR");
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.saved_to_album), Snackbar.LENGTH_LONG).show();

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(GeneratorResultActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                MainActivity.storagePermissionDenied(GeneratorResultActivity.this);

            } else {
                ActivityCompat.requestPermissions(GeneratorResultActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

            }
        }
    }

    public static void saveImage(Bitmap bitmap, Context context, String folderName) {
        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues values = contentValues();
            values.put("relative_path", "Pictures/" + folderName);
            values.put("is_pending", true);
            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try {
                    saveImageToStream(bitmap, context.getContentResolver().openOutputStream(uri));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                values.put("is_pending", false);
                context.getContentResolver().update(uri, values, null, null);
            }
        } else {
            File directory = new File(Environment.getExternalStorageDirectory().toString() + File.separator + folderName);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = System.currentTimeMillis() + ".png";
            File file = new File(directory, fileName);
            try {
                saveImageToStream(bitmap, new FileOutputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            file.getAbsolutePath();
            ContentValues values = contentValues();
            values.put("_data", file.getAbsolutePath());
            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }

    }

    public static ContentValues contentValues() {
        ContentValues values = new ContentValues();
        values.put("mime_type", "image/png");
        values.put("date_added", System.currentTimeMillis() / (long)1000);
        values.put("datetaken", System.currentTimeMillis());
        return values;
    }

    public static void saveImageToStream(Bitmap bitmap, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        }

    }


}
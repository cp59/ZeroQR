package com.zeroapp.zeroqr;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreference;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends Fragment{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getChildFragmentManager().beginTransaction().replace(R.id.settingsFrameLayout,new PreferenceFragment()).commit();
    }
    private static void tintIcons(Preference preference, int color) {
        if (preference instanceof PreferenceGroup) {
            PreferenceGroup group = ((PreferenceGroup) preference);
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                tintIcons(group.getPreference(i), color);
            }
        } else {
            Drawable icon = preference.getIcon();
            if (icon != null) {
                DrawableCompat.setTint(icon, color);
            }
        }
    }
    public static class PreferenceFragment extends PreferenceFragmentCompat {
        private static final String DataBaseName = "HistoryDataBase";
        private static final int DataBaseVersion = 1;
        private static final String DataBaseTable = "History";
        private static SQLiteDatabase db;
        private HistorySQLDataBaseHelper historySQLDataBaseHelper;
        private SharedPreferences settings;
        private SharedPreferences.Editor editor;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            historySQLDataBaseHelper = new HistorySQLDataBaseHelper(getActivity(),DataBaseName,null,DataBaseVersion,DataBaseTable);
            db = historySQLDataBaseHelper.getWritableDatabase();
            int colorAttr = android.R.attr.textColorSecondary;
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(new int[]{colorAttr});
            int iconColor = ta.getColor(0, 0);
            ta.recycle();
            tintIcons(getPreferenceScreen(), iconColor);
            settings = getActivity().getApplicationContext().getSharedPreferences("ZeroQR", 0);
            editor = settings.edit();
            ListPreference startupModeList = findPreference("startupMode");
            ListPreference maxHistorySaveNumberList = findPreference("maxHistorySaveNumber");
            if (settings.getInt("startupMode",0)==0){
                startupModeList.setValueIndex(0);
            } else {
                startupModeList.setValueIndex(1);
            }
            if (settings.getInt("maxHistorySaveNumber",0)==0) {
                maxHistorySaveNumberList.setValueIndex(14);
            } else {
                maxHistorySaveNumberList.setValue(String.valueOf(settings.getInt("maxHistorySaveNumber",0)));
            }
            startupModeList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ListPreference listPreference=(ListPreference)preference;
                    int index=listPreference.findIndexOfValue((String)newValue);
                    Integer startupValue = index;
                    if (startupValue == 0) {
                        editor.putInt("startupMode",0);

                    } else if (startupValue == 1){
                        editor.putInt("startupMode",1);
                    }
                    editor.apply();
                    return true;
                }
            });
            CheckBoxPreference openLinkInExternalBrowser  = findPreference("openLinkInExternalBrowser");
            openLinkInExternalBrowser.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean isChecked = (Boolean) newValue;
                    editor.putBoolean("openLinkInExternalBrowser",isChecked);
                    editor.apply();
                    return true;
                }
            });
            SwitchPreference saveScanHistory = findPreference("saveScanHistory");
            saveScanHistory.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean saveScanHistoryEnable = (Boolean) newValue;
                    editor.putBoolean("saveScanHistory", saveScanHistoryEnable);
                    editor.apply();
                    return true;
                }
            });
            SwitchPreference saveCreateHistory = findPreference("saveCreateHistory");
            saveCreateHistory.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean saveCreateHistoryEnable = (Boolean) newValue;
                    editor.putBoolean("saveCreateHistory", saveCreateHistoryEnable);
                    editor.apply();
                    return true;
                }
            });
            maxHistorySaveNumberList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ListPreference listPreference=(ListPreference)preference;
                    int index=listPreference.findIndexOfValue((String)newValue);
                    Integer saveNumberValue = index;
                    Integer maxHistorySaveNumber = Integer.parseInt((String) newValue);
                    if (saveNumberValue == 14) {
                        editor.putInt("maxHistorySaveNumber",0);
                    } else{
                        editor.putInt("maxHistorySaveNumber", maxHistorySaveNumber);
                        Integer historyNumber = (int) DatabaseUtils.queryNumEntries(db, "History");
                        if (historyNumber>maxHistorySaveNumber) {
                            int needToDelete = historyNumber - maxHistorySaveNumber;
                            db.execSQL("Delete from History where _id IN (Select _id from History limit "+needToDelete+");\n");
                        }
                    }
                    editor.apply();
                    return true;
                }
            });
            Preference clearHistory = findPreference("clearHistory");
            clearHistory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.clear_history_dialog_title))
                            .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.execSQL("DROP TABLE History");
                                    String SqlTable = "CREATE TABLE IF NOT EXISTS History (" +
                                            "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                            "source text not null," +
                                            "type text not null," +
                                            "content text not null," +
                                            "parsedContent text not null," +
                                            "time text not null" +
                                            ")";
                                    db.execSQL(SqlTable);
                                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.clear_history_successfully), Snackbar.LENGTH_LONG);
                                    View snackbarLayout = snackbar.getView();
                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                    );
                                    lp.setMargins(0, 10, 0, 0);
                                    snackbarLayout.setLayoutParams(lp);
                                    snackbar.show();
                                }
                            })
                            .setNegativeButton(getString(android.R.string.cancel),null)
                            .show();
                    return true;
                }
            });
            SwitchPreference copy = findPreference("copy");
            copy.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean copyEnable = (Boolean) newValue;
                    editor.putBoolean("copy", copyEnable);
                    editor.apply();
                    return true;
                }
            });
            SwitchPreference vibrate = findPreference("vibrate");
            vibrate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean vibrateEnable = (Boolean) newValue;
                    editor.putBoolean("vibrate", vibrateEnable);
                    editor.apply();
                    return true;
                }
            });
            SwitchPreference beep = findPreference("beep");
            beep.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean beepEnable = (Boolean) newValue;
                    editor.putBoolean("beep", beepEnable);
                    editor.apply();
                    return true;
                }
            });
            Preference help = findPreference("help");
            help.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String url = "https://sites.google.com/view/zqrhelp/"+getString(R.string.lang);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                    return true;
                }
            });
            Preference reportError = findPreference("reportError");
            reportError.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String url = "https://forms.gle/CGukJchmfJ22MoGY8";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                    return true;
                }
            });
            Preference openSourceLicenses = findPreference("openSourceLicenses");
            openSourceLicenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    OssLicensesMenuActivity.setActivityTitle(getString(R.string.open_source_licenses));
                    startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
                    return true;
                }
            });
            Preference about = findPreference("about");
            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    return true;
                }
            });
        }
    }
}
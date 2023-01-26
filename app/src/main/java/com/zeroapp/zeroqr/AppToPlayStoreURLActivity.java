package com.zeroapp.zeroqr;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppToPlayStoreURLActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_to_play_store_url_activity);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(view -> onBackPressed());
        CardView loadingAppsCard = findViewById(R.id.loadingAppsCard);
        ArrayList<String> appPkgNameLists = new ArrayList<>();
        ListView appListView = findViewById(R.id.appListView);
        appListView.setOnItemClickListener((adapterView, view, i, l) -> {

            Intent intent = new Intent(AppToPlayStoreURLActivity.this, GeneratorResultActivity.class);
            intent.putExtra("content", "https://play.google.com/store/apps/details?id="+appPkgNameLists.get(i));
            startActivity(intent);
            finish();
        });
        ArrayList<Drawable> appIconLists = new ArrayList<>();
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            int appIndex = 0;
            List<Map<String, String>> appLists = new ArrayList<>();
            PackageManager pm = getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(0);
            if (android.os.Build.VERSION.SDK_INT >= 24){
                Collections.sort(packages, Comparator.comparing(packageInfo -> packageInfo.applicationInfo.loadLabel(pm).toString()));
            } else{
                Collections.sort(packages, (packageInfo, t1) -> packageInfo.applicationInfo.loadLabel(pm).toString().compareTo(t1.applicationInfo.loadLabel(pm).toString()));
            }
            for (PackageInfo packageInfo : packages) {
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    HashMap<String, String> app = new HashMap<>();
                    app.put("appName", packageInfo.applicationInfo.loadLabel(pm).toString());
                    app.put("pkgName", packageInfo.packageName);
                    app.put("icon", String.valueOf(appIndex));
                    appIconLists.add(packageInfo.applicationInfo.loadIcon(pm));
                    appPkgNameLists.add(packageInfo.packageName);
                    appLists.add(app);
                    appIndex++;
                }
            }
            SimpleAdapter adapter = new SimpleAdapter(AppToPlayStoreURLActivity.this,appLists,R.layout.installed_app_list,new String[]{"appName","pkgName","icon"},new int[]{R.id.appName,R.id.appPackageName,R.id.appIcon});
            adapter.setViewBinder((view, o, s) -> {
                if (view.getId() == R.id.appIcon) {
                    ((ImageView) view).setImageDrawable(appIconLists.get(Integer.parseInt(String.valueOf(o))));
                    return true;
                }
                return false;
            });
            appListView.setAdapter(adapter);
            appListView.setVisibility(View.VISIBLE);
            loadingAppsCard.setVisibility(View.GONE);
        }, 100);
    }
}
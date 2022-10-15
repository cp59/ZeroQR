package com.zeroapp.zeroqr;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CardView loadingAppsCard = findViewById(R.id.loadingAppsCard);
        ArrayList appPkgNameLists = new ArrayList();
        ListView appListView = findViewById(R.id.appListView);
        appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(AppToPlayStoreURLActivity.this, GeneratorResultActivity.class);
                intent.putExtra("content", "https://play.google.com/store/apps/details?id="+appPkgNameLists.get(i));
                startActivity(intent);
                finish();
            }
        });
        ArrayList appIconLists = new ArrayList();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                int appIndex = 0;
                List<Map<String, String>> appLists = new ArrayList<>();
                PackageManager pm = getPackageManager();
                List<PackageInfo> packages = pm.getInstalledPackages(0);
                Collections.sort(packages, new Comparator<PackageInfo>() {
                    @Override
                    public int compare(PackageInfo packageInfo, PackageInfo t1) {
                        return packageInfo.applicationInfo.loadLabel(pm).toString().compareTo(t1.applicationInfo.loadLabel(pm).toString());
                    }
                });
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
                        ((ImageView) view).setImageDrawable((Drawable) appIconLists.get(Integer.parseInt(String.valueOf(o))));
                        return true;
                    }
                    return false;
                });
                appListView.setAdapter(adapter);
                appListView.setVisibility(View.VISIBLE);
                loadingAppsCard.setVisibility(View.GONE);
            }
        }, 100);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
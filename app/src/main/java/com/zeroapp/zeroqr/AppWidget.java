package com.zeroapp.zeroqr;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class AppWidget extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Intent createIntent = new Intent(context, MainActivity.class);
            createIntent.setAction("com.zeroapp.zeroqr.create");
            PendingIntent createPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    createIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            Intent scanIntent = new Intent(context, MainActivity.class);
            scanIntent.setAction("com.zeroapp.zeroqr.scan");
            PendingIntent scanPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    scanIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
            views.setOnClickPendingIntent(R.id.widget_create_button, createPendingIntent);
            views.setOnClickPendingIntent(R.id.widget_scan_button, scanPendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
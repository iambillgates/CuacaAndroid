package com.dwiakbar.cuaca;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;

public class WeatherWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE);
        String savedCity = prefs.getString("city", "Jakarta");
        String savedTemp = prefs.getString("temp", "--°C");
        String savedCondition = prefs.getString("condition", "Clear");
        String iconCode = prefs.getString("icon_code", "01d"); // Get saved icon code

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget_layout);

        // 1. Map Text Values
        views.setTextViewText(R.id.widget_city_text, savedCity);
        views.setTextViewText(R.id.widget_temp_text, savedTemp);
        views.setTextViewText(R.id.widget_condition_text, savedCondition);

        // 2. Construct the direct image URL from OpenWeatherMap servers
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

        // 3. Create an asynchronous Glide target bound to the widget UI thread context
        AppWidgetTarget awt = new AppWidgetTarget(context, R.id.widget_icon, views, appWidgetId);

        // 4. Stream and inject the network asset directly into your layout image slot
        Glide.with(context.getApplicationContext())
                .asBitmap()
                .load(iconUrl)
                .into(awt);
    }
}
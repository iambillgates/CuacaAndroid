package com.dwiakbar.cuaca;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
{
    private MaterialButton _buttonView_cityInfo, _btnTampilkan;
    private EditText _etKota;
    private RecyclerView _recyclerView1;
    private SwipeRefreshLayout _swipeRefreshLayout1;
    private TextView _totalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _buttonView_cityInfo = findViewById(R.id.buttonView_cityInfo);
        _btnTampilkan = findViewById(R.id.btnTampilkan);
        _etKota = findViewById(R.id.etKota);
        _recyclerView1 = findViewById(R.id.recyclerView1);
        _swipeRefreshLayout1 = findViewById(R.id.swipeRefreshLayout1);
        _totalTextView = findViewById(R.id.totalTextView);

        init_btnTampilkan();
        init_swipeRefreshLayout();

        _btnTampilkan.performClick();
    }

    private void init_swipeRefreshLayout()
    {
        _swipeRefreshLayout1.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                init_btnTampilkan();
            }
        });
    }

    private void init_btnTampilkan()
    {
        _btnTampilkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _swipeRefreshLayout1.setRefreshing(true);

                String kota = _etKota.getText().toString();
                String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + kota + "&appid=67f7f49f9fc7faea9580f7220ad08697";

                AsyncHttpClient ahc = new AsyncHttpClient();

                ahc.get(url, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        _swipeRefreshLayout1.setRefreshing(false);

                        String json = new String(responseBody);
                        Log.d("*tw*", json);

                        Gson gson = new Gson();
                        RootModel rm = gson.fromJson(json, RootModel.class);

                        _totalTextView.setText("Total Record : " + rm.getListModelList().size());

                        LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
                        _recyclerView1.setLayoutManager(llm);

                        CuacaAdapter ca = new CuacaAdapter(rm);
                        _recyclerView1.setAdapter(ca);

                        String cityName = rm.getCityModel().getName();
                        String negara = "ID"; // Default or dynamic if available
                        double lat = rm.getCityModel().getCoordModel().getLat();
                        double lon = rm.getCityModel().getCoordModel().getLon();

                        _buttonView_cityInfo.setText(cityName + ", " + negara + " (Lat: " + lat + ", Lon: " + lon + ")");

                        // --- NEW WIDGET SYNC LOGIC ---
                        // --- UPDATED WIDGET SYNC LOGIC ---
                        if (rm.getListModelList() != null && !rm.getListModelList().isEmpty()) {
                            // 1. Fetch temp range
                            double tempMinCelsius = rm.getListModelList().get(0).getMainModel().getTemp_min() - 273.15;
                            double tempMaxCelsius = rm.getListModelList().get(0).getMainModel().getTemp_max() - 273.15;
                            String formattedTempRange = String.format("%.1f - %.1f°C", tempMinCelsius, tempMaxCelsius);

                            // 2. Fetch condition text AND the network icon code string
                            String weatherCondition = "Clear";
                            String iconCode = "01d"; // Default fallback icon code (clear sky day)

                            if (rm.getListModelList().get(0).getWeatherModelList() != null &&
                                    !rm.getListModelList().get(0).getWeatherModelList().isEmpty()) {

                                weatherCondition = rm.getListModelList().get(0).getWeatherModelList().get(0).getMain();
                                iconCode = rm.getListModelList().get(0).getWeatherModelList().get(0).getIcon(); // GET ICON CODE
                            }

                            // 3. Save to SharedPreferences and trigger Widget broadcast
                            updateWidgetData(cityName, formattedTempRange, weatherCondition, iconCode);
                        }
                        // ------------------------------

                        init_buttonView_cityInfo(rm);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        _swipeRefreshLayout1.setRefreshing(false);
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Helper method to update the SharedPreferences cache and notify WidgetProvider
    private void updateWidgetData(String cityName, String temperature, String condition, String iconCode) {
        SharedPreferences prefs = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("city", cityName);
        editor.putString("temp", temperature);
        editor.putString("condition", condition);
        editor.putString("icon_code", iconCode); // Save here
        editor.apply();

        Intent intent = new Intent(this, WeatherWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), WeatherWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

        sendBroadcast(intent);
    }

    private void init_buttonView_cityInfo(RootModel rm)
    {
        _buttonView_cityInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GpsActivity.class);

                Bundle bundle = new Bundle();
                bundle.putDouble("lat", rm.getCityModel().getCoordModel().getLat());
                bundle.putDouble("lon", rm.getCityModel().getCoordModel().getLon());

                intent.putExtra("param", bundle);

                startActivity(intent);
            }
        });
    }
}
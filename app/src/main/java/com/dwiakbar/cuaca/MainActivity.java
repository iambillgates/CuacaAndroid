package com.dwiakbar.cuaca;

import android.content.Intent;
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
package com.dwiakbar.cuaca;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GpsActivity extends AppCompatActivity {

    private TextView _koordinatTextView;
    private WebView _webView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        Bundle param = getIntent().getBundleExtra("param");

        _koordinatTextView = findViewById(R.id.textView_koordinat);

        if (param != null) {
            double lat = param.getDouble("lat");
            double lon = param.getDouble("lon");

            _koordinatTextView.setText(lat + " X " + lon);

            _webView1 = findViewById(R.id.wvMain);

            // UBAH DISINI: Format URL untuk OpenStreetMap dengan Marker/Pin
            // #map=18/lat/lon adalah tingkat zoom dan pusat peta
            // ?mlat=lat&mlon=lon adalah koordinat untuk memunculkan Pin (Marker) merah
            String url = "https://www.openstreetmap.org/?mlat=" + lat + "&mlon=" + lon + "#map=18/" + lat + "/" + lon;

            WebSettings webSettings = _webView1.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);

            // Karena OSM ramah browser mobile, kita bisa pakai WebViewClient standar tanpa trik UserAgent
            _webView1.setWebViewClient(new WebViewClient());
            _webView1.loadUrl(url);
        }
    }
}
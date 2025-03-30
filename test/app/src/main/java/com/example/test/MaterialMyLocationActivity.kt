package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MaterialMyLocationActivity : AppCompatActivity() {

    private var selectedLat: Double? = null
    private var selectedLng: Double? = null

    @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_location)

        val webView = findViewById<WebView>(R.id.mapWebView)
        val selectBtn = findViewById<Button>(R.id.btn_select)
        val closeBtn = findViewById<ImageView>(R.id.btn_close)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")

        val htmlData = """
            <html>
            <head>
                <meta charset="utf-8">
                <title>Tmap 지도</title>
                <script src="https://apis.openapi.sk.com/tmap/jsv2?version=1&appKey=5p5MIj0ajg149wOnUX3Ui5WfNBUnZngt5kCE8TMc"></script>
                <style>
                    html, body, #map {
                        width: 100%;
                        height: 100%;
                        margin: 0;
                        padding: 0;
                    }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = new Tmapv2.Map("map", {
                        center: new Tmapv2.LatLng(37.5665, 126.9780),
                        width: "100%",
                        height: "100%",
                        zoom: 15
                    });

                    var marker = new Tmapv2.Marker({
                        position: new Tmapv2.LatLng(37.5665, 126.9780),
                        map: map
                    });

                    map.addListener("click", function(evt) {
                        var lat = evt.latLng._lat;
                        var lng = evt.latLng._lng;
                        marker.setPosition(new Tmapv2.LatLng(lat, lng));

                        if (window.AndroidBridge) {
                            window.AndroidBridge.sendLocation(lat, lng);
                        }
                    });
                </script>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(
            "https://apis.openapi.sk.com",
            htmlData,
            "text/html",
            "UTF-8",
            null
        )

        selectBtn.setOnClickListener {
            if (selectedLat != null && selectedLng != null) {
                val intent = Intent(this, MaterialActivity::class.java).apply {
                    putExtra("latitude", selectedLat)
                    putExtra("longitude", selectedLng)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "지도를 클릭해 위치를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        closeBtn.setOnClickListener {
            finish()
        }
    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun sendLocation(lat: Double, lng: Double) {
            selectedLat = lat
            selectedLng = lng
            Log.d("TmapWebView", "위치 전달됨: $lat, $lng")
        }
    }
}

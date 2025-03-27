package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MaterialMyLocationActivity : AppCompatActivity() {

    private var selectedLat: Double? = null
    private var selectedLng: Double? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_location)

        val webView = findViewById<WebView>(R.id.kakaoMapView)
        val selectBtn = findViewById<Button>(R.id.btn_select)

        // WebView 설정
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // Mixed Content 허용
            mediaPlaybackRequiresUserGesture = false
        }


        // WebView JS 에러 로그 확인용
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d("KAKAO_WEBVIEW", consoleMessage.message())
                return true
            }
        }

        // HTML 문자열 데이터
        val htmlData = """
            <html>
            <head>
                <meta charset="utf-8">
                <title>카카오 지도</title>
                <script src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=87c521209afc17360a11c89dcb5a9bc4"></script>
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
                    var mapContainer = document.getElementById('map');
                    var mapOption = {
                        center: new kakao.maps.LatLng(37.5665, 126.9780), // 서울
                        level: 3
                    };

                    var map = new kakao.maps.Map(mapContainer, mapOption);
                    var marker = new kakao.maps.Marker();

                    kakao.maps.event.addListener(map, 'click', function(mouseEvent) {
                        var latlng = mouseEvent.latLng;
                        marker.setPosition(latlng);
                        marker.setMap(map);

                        // Kotlin으로 위치 전달
                        if (window.AndroidBridge) {
                            window.AndroidBridge.sendLocation(latlng.getLat(), latlng.getLng());
                        }
                    });
                </script>
            </body>
            </html>
        """.trimIndent()

        // 로컬 HTML을 WebView에 로드
        webView.loadDataWithBaseURL("https://dapi.kakao.com", htmlData, "text/html", "UTF-8", null)

        // 선택 완료 버튼
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
    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun sendLocation(lat: Double, lng: Double) {
            selectedLat = lat
            selectedLng = lng
            Log.d("KAKAO_WEBVIEW", "위치 전달됨: $lat, $lng")
        }
    }
}

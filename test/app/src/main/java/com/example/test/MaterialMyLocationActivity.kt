package com.example.test

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.network.RetrofitInstance
import com.skt.tmap.TMapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaterialMyLocationActivity : AppCompatActivity() {

    private lateinit var tmapView: TMapView
    private var selectedLat: Double? = null
    private var selectedLng: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_location)

        val mapContainer = findViewById<FrameLayout>(R.id.mapContainer)

        tmapView = TMapView(this)
        tmapView.setSKTMapApiKey("5p5MIj0ajg149wOnUX3Ui5WfNBUnZngt5kCE8TMc")
        mapContainer.addView(tmapView, 0)

        Handler(Looper.getMainLooper()).postDelayed({
            selectedLat = 37.49054682674008
            selectedLng = 126.77845157600154

            try {
                tmapView.setZoomLevel(15)
                tmapView.setCenterPoint(selectedLat!!, selectedLng!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            findViewById<Button>(R.id.btn_zoom_in).setOnClickListener {
                val currentZoom = tmapView.zoomLevel
                tmapView.zoomLevel = (currentZoom + 1).coerceAtMost(20)
            }

            findViewById<Button>(R.id.btn_zoom_out).setOnClickListener {
                val currentZoom = tmapView.zoomLevel
                tmapView.zoomLevel = (currentZoom - 1).coerceAtLeast(1)
            }

        }, 1500)

        // 지도 이동시 항상 중심좌표 업데이트
        tmapView.setOnDisableScrollWithZoomLevelListener { _, centerPoint ->
            selectedLat = centerPoint.latitude
            selectedLng = centerPoint.longitude
        }

        findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_select).setOnClickListener {
            val token = App.prefs.token.toString()
            if (token.isBlank() || token == "null") {
                Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val lat = selectedLat
            val lng = selectedLng

            if (lat == null || lng == null) {
                Toast.makeText(this, "지도를 움직여 위치를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitInstance.apiService.saveUserLocation("Bearer $token", lat, lng)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MaterialMyLocationActivity, "위치 저장 완료", Toast.LENGTH_SHORT).show()
                            // 저장 완료 후 MaterialActivity에 알림
                            val resultIntent = Intent()
                            resultIntent.putExtra("locationSaved", true)
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        } else {
                            Toast.makeText(this@MaterialMyLocationActivity, "저장 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@MaterialMyLocationActivity, "서버 오류 발생", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // 뒤로가기 버튼
        findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            finish()
        }
    }

}

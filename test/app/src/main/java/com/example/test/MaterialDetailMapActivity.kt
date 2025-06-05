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
import com.skt.tmap.TMapView

class MaterialDetailMapActivity : AppCompatActivity() {

    private lateinit var tmapView: TMapView
    private var selectedLat: Double? = null
    private var selectedLng: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_detail_map)

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

        // 🔥 지도 이동시 항상 중심좌표 업데이트
        tmapView.setOnDisableScrollWithZoomLevelListener { _, centerPoint ->
            selectedLat = centerPoint.latitude
            selectedLng = centerPoint.longitude
        }

        findViewById<Button>(R.id.btn_select).setOnClickListener {
            if (selectedLat != null && selectedLng != null) {
                val intent = Intent(this, MaterialActivity::class.java).apply {
                    putExtra("latitude", selectedLat)
                    putExtra("longitude", selectedLng)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "지도를 움직여 위치를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 뒤로가기 버튼
        findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            finish()
        }
    }
}

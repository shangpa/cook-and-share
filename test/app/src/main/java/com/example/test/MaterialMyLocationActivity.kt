package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapMarkerItem

class MaterialMyLocationActivity : AppCompatActivity() {

    private lateinit var tmapView: TMapView
    private var selectedLat: Double? = null
    private var selectedLng: Double? = null
    private val markerId = "selected_marker"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_location)

        val mapContainer = findViewById<FrameLayout>(R.id.mapContainer)

        // TMapView 객체 생성
        tmapView = TMapView(this)
        tmapView.setSKTMapApiKey("5p5MIj0ajg149wOnUX3Ui5WfNBUnZngt5kCE8TMc")
        mapContainer.addView(tmapView)

        val selectBtn = findViewById<Button>(R.id.btn_select)
        val closeBtn = findViewById<ImageView>(R.id.btn_close)



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
}

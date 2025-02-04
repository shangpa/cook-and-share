package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MaterialMyLocationActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_location) // activity_material_my_location 레이아웃 파일 연결

        // btn_select 클릭했을 때 MaterialActivity 이동
        val btn_select: Button = findViewById(R.id.btn_select)
        btn_select.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

    }
}
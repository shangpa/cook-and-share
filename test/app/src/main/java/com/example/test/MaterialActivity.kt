package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MaterialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material) // MaterialActivity의 레이아웃 파일 연결

        // item1을 클릭했을 때 MaterialDetailActivity로 이동
        val item1: LinearLayout = findViewById(R.id.item1)
        item1.setOnClickListener {
            val intent = Intent(this, MaterialDetailActivity::class.java)
            startActivity(intent)
        }
    }
}

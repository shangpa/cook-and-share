package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MaterialSearchActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_search) // MaterialDetailActivity의 레이아웃 파일 연결

        // seach을 클릭했을 때 MaterialSearchDetailActivity 이동
        val seach: LinearLayout = findViewById(R.id.seach)
        seach.setOnClickListener {
            val intent = Intent(this, MaterialSearchDetailActivity::class.java)
            startActivity(intent)
        }
    }
}
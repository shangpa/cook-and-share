package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MaterialReviewActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_received_review) // 다른 프로필 화면의 레이아웃 파일 연결

        // receivedReview 클릭했을 때 MaterialReviewActivity 이동
        val receivedReview: TextView = findViewById(R.id.receivedReview)
        receivedReview.setOnClickListener {
            val intent = Intent(this, MaterialReviewActivity::class.java)
            startActivity(intent)
        }
    }
}

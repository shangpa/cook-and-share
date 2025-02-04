package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MaterialMyProfileActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_profile) // 다른 프로필 화면의 레이아웃 파일 연결

        // ic_rigth1 클릭했을 때 MaterialSalesActivity 이동
        val ic_rigth1: ImageView = findViewById(R.id.ic_rigth1)
        ic_rigth1.setOnClickListener {
            val intent = Intent(this, MaterialSalesActivity::class.java)
            startActivity(intent)
        }

        // ic_rigth2 클릭했을 때 MaterialPurchaseActivity 이동
        val ic_rigth2: ImageView = findViewById(R.id.ic_rigth2)
        ic_rigth2.setOnClickListener {
            val intent = Intent(this, MaterialPurchaseActivity::class.java)
            startActivity(intent)
        }

        // ic_rigth3 클릭했을 때 MaterialSavedActivity 이동
        val ic_rigth3: ImageView = findViewById(R.id.ic_rigth3)
        ic_rigth3.setOnClickListener {
            val intent = Intent(this, MaterialSavedActivity::class.java)
            startActivity(intent)
        }

        // ic_rigth4 클릭했을 때 MaterialReviewActivity 이동
        val ic_rigth4: ImageView = findViewById(R.id.ic_rigth4)
        ic_rigth4.setOnClickListener {
            val intent = Intent(this, MaterialReviewActivity::class.java)
            startActivity(intent)
        }
    }
}

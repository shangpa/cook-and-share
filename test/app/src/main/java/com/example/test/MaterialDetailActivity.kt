package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MaterialDetailActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_detail) // MaterialDetailActivity의 레이아웃 파일 연결

        // item1을 클릭했을 때 MaterialDetailActivity로 이동
        val otherProfile: LinearLayout = findViewById(R.id.otherProfile)
        otherProfile.setOnClickListener {
            val intent = Intent(this, MaterialOtherProfileActivity::class.java)
            startActivity(intent)
        }

        // chatButton 클릭했을 때 MaterialDetailActivity로 이동
        val chatButton: Button = findViewById(R.id.chatButton)
        chatButton.setOnClickListener {
            val intent = Intent(this, MaterialChatActivity::class.java)
            startActivity(intent)
        }
    }
}
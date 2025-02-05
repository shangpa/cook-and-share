package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MaterialChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_chat) // 다른 프로필 화면의 레이아웃 파일 연결

        // chat1 클릭했을 때 MaterialChatDetailActivity 이동
        val chat1: LinearLayout = findViewById(R.id.chat1)
        chat1.setOnClickListener {
            val intent = Intent(this, MaterialChatDetailActivity::class.java)
            startActivity(intent)
        }

        // chatBack 클릭했을 때 MainActivity 이동
        val chatBack: ImageView = findViewById(R.id.chatBack)
        chatBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}

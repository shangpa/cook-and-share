package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MaterialChatDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_chat_detail) // 다른 프로필 화면의 레이아웃 파일 연결

        // chatDetailBack 클릭했을 때 MaterialChatActivity 이동
        val chatDetailBack: ImageView = findViewById(R.id.chatDetailBack)
        chatDetailBack.setOnClickListener {
            val intent = Intent(this, MaterialChatActivity::class.java)
            startActivity(intent)
        }
    }
}

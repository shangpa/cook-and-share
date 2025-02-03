package com.example.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MaterialChatDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_chat_detail) // 다른 프로필 화면의 레이아웃 파일 연결
    }
}

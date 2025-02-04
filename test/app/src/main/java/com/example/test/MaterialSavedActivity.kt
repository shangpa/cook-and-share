package com.example.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MaterialSavedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_saved_transaction) // 다른 프로필 화면의 레이아웃 파일 연결
    }
}

package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MaterialActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material) // MaterialActivity의 레이아웃 파일 연결

        // item1을 클릭했을 때 MaterialDetailActivity로 이동
        val item1: LinearLayout = findViewById(R.id.item1)
        item1.setOnClickListener {
            val intent = Intent(this, MaterialDetailActivity::class.java)
            startActivity(intent)
        }

        // searchIcon을 클릭했을 때 MaterialSearchActivity 이동
        val searchIcon: LinearLayout = findViewById(R.id.searchIcon)
        searchIcon.setOnClickListener {
            val intent = Intent(this, MaterialSearchActivity::class.java)
            startActivity(intent)
        }

        // myLocation을 클릭했을 때 MaterialMyLocationActivity 이동
        val myLocation: LinearLayout = findViewById(R.id.myLocation)
        myLocation.setOnClickListener {
            val intent = Intent(this, MaterialMyLocationActivity::class.java)
            startActivity(intent)
        }

        // profileIcon 클릭했을 때 MaterialMyProfileActivityActivity 이동
        val profileIcon: LinearLayout = findViewById(R.id.profileIcon)
        profileIcon.setOnClickListener {
            val intent = Intent(this, MaterialMyProfileActivity::class.java)
            startActivity(intent)
        }

        // plusIcon3 클릭했을 때 MaterialMyProfileActivityActivity 이동
        val plusIcon3: LinearLayout = findViewById(R.id.plusIcon3)
        plusIcon3.setOnClickListener {
            val intent = Intent(this, MaterialWritingActivity::class.java)
            startActivity(intent)
        }
    }

}

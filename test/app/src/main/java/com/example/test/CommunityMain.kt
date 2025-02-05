/*커뮤니티 메인*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CommunityMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.community_main)

        // popularPost 클릭했을 때 NoticeActivity 이동
        val popularPost: TextView = findViewById(R.id.popularPost)
        popularPost.setOnClickListener {
            val intent = Intent(this, PopularPost::class.java)
            startActivity(intent)
        }

        // freePost 클릭했을 때 NoticeActivity 이동
        val freePost: TextView = findViewById(R.id.freePost)
        freePost.setOnClickListener {
            val intent = Intent(this, FreePost::class.java)
            startActivity(intent)
        }

        // cookPosT 클릭했을 때 NoticeActivity 이동
        val cookPosT: TextView = findViewById(R.id.cookPosT)
        cookPosT.setOnClickListener {
            val intent = Intent(this, CookPost::class.java)
            startActivity(intent)
        }

        // popularPostTwo 클릭했을 때 NoticeActivity 이동
        val popularPostTwo: TextView = findViewById(R.id.popularPostTwo)
        popularPostTwo.setOnClickListener {
            val intent = Intent(this, CommunityDetail::class.java)
            startActivity(intent)
        }

        // bellIcon 클릭했을 때 NoticeActivity 이동
        val bellIcon: ImageButton = findViewById(R.id.bellIcon)
        bellIcon.setOnClickListener {
            val intent = Intent(this, WritePost::class.java)
            startActivity(intent)
        }
    }
}
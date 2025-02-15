/*비디오 상세*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class VideoSee : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_see)

        // delete 클릭했을 때 RecipeWriteVideoContentCheck 이동
        val delete: TextView = findViewById(R.id.delete)
        delete.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoContentCheck::class.java)
            startActivity(intent)
        }

        // modify 클릭했을 때 RecipeWriteVideoDetailSettle 이동
        val modify: TextView = findViewById(R.id.modify)
        modify.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoDetailSettle::class.java)
            startActivity(intent)
        }

        // 비워진 하트와 채워진 하트 선언
        val heart = findViewById<ImageButton>(R.id.heart)
        val heartFill = findViewById<ImageButton>(R.id.heartFill)

        // 비워진 하트 눌렀을때 비워진 하트 없어지고 채워진 하트 나타남
        heart.setOnClickListener {
            heart.visibility = View.GONE
            heartFill.visibility = View.VISIBLE
        }

        // 채워진 하트 눌렀을때 채워진 하트 없어지고 비워진 하트 나타남
        heartFill.setOnClickListener {
            heartFill.visibility = View.GONE
            heart.visibility = View.VISIBLE
        }

        // 비워진 좋아요와 채워진 좋아요 선언
        val good = findViewById<ImageButton>(R.id.good)
        val goodFill = findViewById<ImageButton>(R.id.goodFill)

        // 비워진 좋아요 눌렀을때 비워진 좋아요 없어지고 채워진 좋아요 나타남
        good.setOnClickListener {
            good.visibility = View.GONE
            goodFill.visibility = View.VISIBLE
        }

        // 채워진 좋아요 눌렀을때 채워진 좋아요 없어지고 비워진 좋아요 나타남
        goodFill.setOnClickListener {
            goodFill.visibility = View.GONE
            good.visibility = View.VISIBLE
        }

        // 더하기 상자 선언
        val add = findViewById<ImageButton>(R.id.add)
        val addBox = findViewById<View>(R.id.addBox)
        val categoryBoxBar = findViewById<View>(R.id.categoryBoxBar)

        // 더하기 눌렀을때 더하기 상자 짜라잔
        add.setOnClickListener {
            addBox.visibility = View.VISIBLE
            modify.visibility = View.VISIBLE
            categoryBoxBar.visibility = View.VISIBLE
            delete.visibility = View.VISIBLE
        }

        // 이전 화면으로 이동
        val backArrow: ImageButton = findViewById(R.id.backArrow)
        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
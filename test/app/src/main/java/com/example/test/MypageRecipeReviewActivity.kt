package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class MypageRecipeReviewActivity : AppCompatActivity() {

    private var selectedCategoryButton: AppCompatButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_recipe_review)

        setupCategoryButtons()
    }

    private fun setupCategoryButtons() {
        val categoryButtons = listOf(
            findViewById<AppCompatButton>(R.id.allBtn),
            findViewById(R.id.krBtn),
            findViewById(R.id.wsBtn),
            findViewById(R.id.jpBtn),
            findViewById(R.id.cnBtn),
            findViewById(R.id.vgBtn),
            findViewById(R.id.snBtn),
            findViewById(R.id.asBtn),
            findViewById(R.id.sdBtn)
        )

        categoryButtons.forEach { button ->
            button.setOnClickListener {
                // 기존 선택된 버튼 초기화
                selectedCategoryButton?.apply {
                    setBackgroundResource(R.drawable.btn_recipe_add)
                    setTextColor(Color.parseColor("#8A8F9C"))
                }

                // 선택된 버튼 스타일 적용 (모양 유지)
                button.setBackgroundResource(R.drawable.btn_fridge_ct_selected)
                button.setTextColor(Color.WHITE)

                selectedCategoryButton = button
            }
        }

        // backButton 클릭했을 때 MypageActivity 이동
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }
    }
}

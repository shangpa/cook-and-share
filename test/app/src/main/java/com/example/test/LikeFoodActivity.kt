package com.example.test

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LikeFoodActivity : AppCompatActivity() {

    private val selectedFoods = mutableSetOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like_food)

        val LikeFoodBackButton: ImageView = findViewById(R.id.LikeFoodBackButton)
        LikeFoodBackButton.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        // 각 요리 항목에 클릭 리스너 등록
        val foodLayouts = listOf(
            findViewById<LinearLayout>(R.id.koreanFood),
            findViewById<LinearLayout>(R.id.westernFood),
            findViewById<LinearLayout>(R.id.japaneseFood),
            findViewById<LinearLayout>(R.id.chineseFood),
            findViewById<LinearLayout>(R.id.vegetarianFood),
            findViewById<LinearLayout>(R.id.snacksFood),
            findViewById<LinearLayout>(R.id.snacks),
            findViewById<LinearLayout>(R.id.sideDishes)
        )

        for (layout in foodLayouts) {
            layout.setOnClickListener {
                toggleSelection(layout)
            }
        }
    }

    private fun toggleSelection(view: View) {
        val background = view.background as GradientDrawable
        val selectedColor = ContextCompat.getColor(this, R.color.green_35A825_20)
        val selectedStroke = ContextCompat.getColor(this, R.color.green_35A825)

        if (selectedFoods.contains(view)) {
            // 선택 해제
            selectedFoods.remove(view)
            background.setColor(ContextCompat.getColor(this, android.R.color.transparent))
            background.setStroke(3, ContextCompat.getColor(this, R.color.gray_D9D9D9))
        } else {
            // 선택
            selectedFoods.add(view)
            background.setColor(selectedColor)
            background.setStroke(3, selectedStroke)
        }
    }
}

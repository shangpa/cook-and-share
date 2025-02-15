/*레시피 단계 스크롤*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StepRecipeScroll : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step_recipe_see_scroll_main)

        // material 클릭했을 때 StepRecipeNoTimer 이동
        val material: TextView = findViewById(R.id.material)
        material.setOnClickListener {
            val intent = Intent(this, StepRecipeNoTimer::class.java)
            startActivity(intent)
        }

        // review 클릭했을 때 StepRecipeReview 이동
        val review: TextView = findViewById(R.id.review)
        review.setOnClickListener {
            val intent = Intent(this, StepRecipeReview::class.java)
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
    }
}
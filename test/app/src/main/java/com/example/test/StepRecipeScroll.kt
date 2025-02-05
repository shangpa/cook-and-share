/*레시피 단계 스크롤*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StepRecipeScroll : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step_recipe_see_scroll_main)

        // review 클릭했을 때 NoticeActivity 이동
        val review: TextView = findViewById(R.id.review)
        review.setOnClickListener {
            val intent = Intent(this, StepRecipeReview::class.java)
            startActivity(intent)
        }
    }
}
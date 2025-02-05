/*레시피 단계 타이머 없음*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StepRecipeNoTimer : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step_recipe_see_main_no_timer)

        // cookOrder 클릭했을 때 NoticeActivity 이동
        val cookOrder: TextView = findViewById(R.id.cookOrder)
        cookOrder.setOnClickListener {
            val intent = Intent(this, StepRecipeScroll::class.java)
            startActivity(intent)
        }

        // backArrow 클릭했을 때 NoticeActivity 이동
        val backArrow: ImageButton = findViewById(R.id.backArrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
/*레시피 단계 STEP6*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class StepRecipeSix : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_recipe_see6)

        // nextFixButton 클릭했을 때 StepRecipeSeven 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, StepRecipeSeven::class.java)
            startActivity(intent)
        }

        // endFixButton 클릭했을 때 RecipeMainOne 이동
        val endFixButton: Button = findViewById(R.id.endFixButton)
        endFixButton.setOnClickListener {
            val intent = Intent(this, RecipeMainOne::class.java)
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
/*레시피 단계 STEP7*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class StepRecipeSeven : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_recipe_see7)

        // nextFixButton 클릭했을 때 RecipeComplete 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeComplete::class.java)
            startActivity(intent)
        }

        // 하트 버튼과 채워진 하트 버튼 선언
        val heartButton = findViewById<ImageButton>(R.id.heart)
        val heartFillButton = findViewById<ImageButton>(R.id.heartFill)

        // 하트 버튼 클릭 리스너 설정
        heartButton.setOnClickListener {
            // 하트 버튼을 숨기고 채워진 하트 버튼을 보이게 설정
            heartButton.visibility = View.GONE
            heartFillButton.visibility = View.VISIBLE
        }

        // 채워진 하트 클릭시 다시 그냥 하트로 돌아감
        heartFillButton.setOnClickListener {
            // 채워진 하트 버튼을 숨기고 하트 버튼을 보이게 설정
            heartFillButton.visibility = View.GONE
            heartButton.visibility = View.VISIBLE
        }

        // 좋아요 버튼과 좋아요 하트 버튼 선언
        val bellIcon = findViewById<ImageButton>(R.id.bellIcon)
        val bellFill = findViewById<ImageButton>(R.id.bellFill)

        // 좋아요 버튼 클릭 리스너 설정
        bellIcon.setOnClickListener {
            // 하트 버튼을 숨기고 채워진 하트 버튼을 보이게 설정
            bellIcon.visibility = View.GONE
            bellFill.visibility = View.VISIBLE
        }

        // 채워진 좋아요 클릭시 다시 그냥 좋아요로 돌아감
        bellFill.setOnClickListener {
            // 채워진 하트 버튼을 숨기고 하트 버튼을 보이게 설정
            bellFill.visibility = View.GONE
            bellIcon.visibility = View.VISIBLE
        }
    }
}
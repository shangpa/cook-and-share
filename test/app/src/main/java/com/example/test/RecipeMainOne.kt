/*레시피 3가지 선택지 다 있음(타이머 유무, 동영상)*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class RecipeMainOne : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipe_main1)

        // rigthArrowThree 클릭했을 때 NoticeActivity 이동
        val rigthArrowThree: View = findViewById(R.id.rigthArrowThree)
        rigthArrowThree.setOnClickListener {
            val intent = Intent(this, VideoSee::class.java)
            startActivity(intent)
        }

        // rigthArrow 클릭했을 때 NoticeActivity 이동
        val rigthArrow: View = findViewById(R.id.rigthArrow)
        rigthArrow.setOnClickListener {
            val intent = Intent(this, RecipeNumberOfPeople::class.java)
            startActivity(intent)
        }

        // rigthArrowTwo 클릭했을 때 NoticeActivity 이동
        val rigthArrowTwo: View = findViewById(R.id.rigthArrowTwo)
        rigthArrowTwo.setOnClickListener {
            val intent = Intent(this, StepRecipeNoTimer::class.java)
            startActivity(intent)
        }
    }
}
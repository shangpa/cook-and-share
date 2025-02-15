/*레시피 2가지 선택지 있음(타이머 유무), (예상 사용재료 없음)*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeMainFour : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipe_main4)

        // yes 클릭했을 때 StepRecipeMain 이동
        val yes: Button = findViewById(R.id.yes)
        yes.setOnClickListener {
            val intent = Intent(this, StepRecipeMain::class.java)
            startActivity(intent)
        }

        // searchIcon 클릭했을 때 SearchMain 이동
        val searchIcon: ImageButton = findViewById(R.id.searchIcon)
        searchIcon.setOnClickListener {
            val intent = Intent(this, SearchMain::class.java)
            startActivity(intent)
        }

        // bellIcon 클릭했을 때 NoticeActivity 이동
        val bellIcon: ImageButton = findViewById(R.id.bellIcon)
        bellIcon.setOnClickListener {
            val intent = Intent(this, NoticeActivity::class.java)
            startActivity(intent)
        }

        // person 클릭했을 때 MypageActivity 이동
        val person: ImageButton = findViewById(R.id.person)
        person.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        // viewWithTimer 클릭했을 때 StepRecipeMain 이동
        val viewWithTimer: TextView = findViewById(R.id.viewWithTimer)
        viewWithTimer.setOnClickListener {
            val intent = Intent(this, StepRecipeMain::class.java)
            startActivity(intent)
        }

        // rigthArrow 클릭했을 때 StepRecipeMain 이동
        val rigthArrow: ImageButton = findViewById(R.id.rigthArrow)
        rigthArrow.setOnClickListener {
            val intent = Intent(this, StepRecipeMain::class.java)
            startActivity(intent)
        }

        // viewWithoutTimer 클릭했을 때 StepRecipeNoTimer 이동
        val viewWithoutTimer: TextView = findViewById(R.id.viewWithoutTimer)
        viewWithoutTimer.setOnClickListener {
            val intent = Intent(this, StepRecipeNoTimer::class.java)
            startActivity(intent)
        }

        // rigthArrowTwo 클릭했을 때 StepRecipeMain 이동
        val rigthArrowTwo: ImageButton = findViewById(R.id.rigthArrowTwo)
        rigthArrowTwo.setOnClickListener {
            val intent = Intent(this, StepRecipeNoTimer::class.java)
            startActivity(intent)
        }

        // 회색 배경, 냉장고 재료 관리하러 가기 버튼, 상자 선언
        val buyFixButton = findViewById<Button>(R.id.buyFixButton)
        val dimView = findViewById<View>(R.id.dimView)
        val materialUseBox = findViewById<View>(R.id.materialUseBox)
        val materialUse = findViewById<TextView>(R.id.materialUse)
        val no = findViewById<Button>(R.id.no)

        // 회색 배경 보이기
        buyFixButton.setOnClickListener {
            dimView.visibility = View.VISIBLE
            materialUseBox.visibility = View.VISIBLE
            materialUse.visibility = View.VISIBLE
            no.visibility = View.VISIBLE
            yes.visibility = View.VISIBLE
        }

        // 회색 배경 보이기
        no.setOnClickListener {
            dimView.visibility = View.GONE
            materialUseBox.visibility = View.GONE
            materialUse.visibility = View.GONE
            no.visibility = View.GONE
            yes.visibility = View.GONE
        }
    }
}
/*레시피 작성 글 완료*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeWriteBothContentCheck : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_both_content_check)

        // nextFixButton 클릭했을 때 StepRecipeNoTimer 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, StepRecipeNoTimer::class.java)
            startActivity(intent)
        }

        // register 클릭했을 때 StepRecipeNoTimer 이동
        val register: Button = findViewById(R.id.register)
        register.setOnClickListener {
            val intent = Intent(this, StepRecipeNoTimer::class.java)
            startActivity(intent)
        }

        // 선언
        val uncheck = findViewById<ImageButton>(R.id.uncheck)
        val checkFill = findViewById<ImageButton>(R.id.checkFill)
        val uncheckTwo = findViewById<ImageButton>(R.id.uncheckTwo)
        val checkFillTwo = findViewById<ImageButton>(R.id.checkFillTwo)
        val buyFixButton = findViewById<Button>(R.id.buyFixButton)
        val shareSettleBox = findViewById<View>(R.id.shareSettleBox)
        val shareSettleTitle = findViewById<TextView>(R.id.shareSettleTitle)
        val totalShare = findViewById<TextView>(R.id.totalShare)
        val justLookMe = findViewById<TextView>(R.id.justLookMe)
        val cancel = findViewById<TextView>(R.id.cancel)
        val settle = findViewById<TextView>(R.id.settle)
        val dimView = findViewById<View>(R.id.dimView)
        val recipeRegisterBox = findViewById<View>(R.id.recipeRegisterBox)
        val recipeRegister = findViewById<TextView>(R.id.recipeRegister)
        val cancelTwo = findViewById<Button>(R.id.cancelTwo)

        // 공개설정 눌렀을때 공유설정 상자 짜라잔
        buyFixButton.setOnClickListener {
            shareSettleBox.visibility = View.VISIBLE
            shareSettleTitle.visibility = View.VISIBLE
            totalShare.visibility = View.VISIBLE
            uncheck.visibility = View.VISIBLE
            justLookMe.visibility = View.VISIBLE
            uncheckTwo.visibility = View.VISIBLE
            cancel.visibility = View.VISIBLE
            settle.visibility = View.VISIBLE
            dimView.visibility = View.VISIBLE
        }

        // 미선택1 눌렀을때 선택1 짜라잔
        uncheck.setOnClickListener {
            checkFill.visibility = View.VISIBLE
        }

        // 미선택2 눌렀을때 선택2 짜라잔
        uncheckTwo.setOnClickListener {
            checkFillTwo.visibility = View.VISIBLE
        }

        // 선택1 눌렀을때 미선택1 짜라잔
        checkFill.setOnClickListener {
            uncheck.visibility = View.VISIBLE
        }

        // 선택2 눌렀을때 미선택2 짜라잔
        checkFillTwo.setOnClickListener {
            uncheckTwo.visibility = View.VISIBLE
        }

        // 취소 눌렀을때 공유설정 상자 사라짐
        cancel.setOnClickListener {
            shareSettleBox.visibility = View.GONE
            shareSettleTitle.visibility = View.GONE
            totalShare.visibility = View.GONE
            uncheck.visibility = View.GONE
            justLookMe.visibility = View.GONE
            uncheckTwo.visibility = View.GONE
            cancel.visibility = View.GONE
            settle.visibility = View.GONE
            dimView.visibility = View.GONE
            checkFill.visibility = View.GONE
            checkFillTwo.visibility = View.GONE
        }

        // 설정 눌렀을때 레시피 등록 상자 짜라잔
        settle.setOnClickListener {
            recipeRegisterBox.visibility = View.VISIBLE
            recipeRegister.visibility = View.VISIBLE
            cancelTwo.visibility = View.VISIBLE
            register.visibility = View.VISIBLE


            shareSettleBox.visibility = View.GONE
            shareSettleTitle.visibility = View.GONE
            totalShare.visibility = View.GONE
            uncheck.visibility = View.GONE
            justLookMe.visibility = View.GONE
            uncheckTwo.visibility = View.GONE
            cancel.visibility = View.GONE
            settle.visibility = View.GONE
            checkFill.visibility = View.GONE
            checkFillTwo.visibility = View.GONE
        }

        // 레시피 등록 상자에서 취소 눌렀을때 레시피 등록 상자 사라짐
        cancelTwo.setOnClickListener {
            recipeRegisterBox.visibility = View.GONE
            recipeRegister.visibility = View.GONE
            cancelTwo.visibility = View.GONE
            register.visibility = View.GONE
            dimView.visibility = View.GONE
        }

        // 이전 화면으로 이동
        val backArrow: ImageButton = findViewById(R.id.backArrow)
        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
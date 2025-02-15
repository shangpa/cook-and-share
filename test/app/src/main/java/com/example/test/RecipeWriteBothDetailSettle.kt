/*레시피 작성 STEP7*/
package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class RecipeWriteBothDetailSettle : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_both_detail_settle)

        // nextFixButton 클릭했을 때 RecipeWriteBothContentCheck 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothContentCheck::class.java)
            startActivity(intent)
        }

        // skipFixButton 클릭했을 때 RecipeWriteBothCookVideo 이동
        val skipFixButton: Button = findViewById(R.id.skipFixButton)
        skipFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothCookVideo::class.java)
            startActivity(intent)
        }

        // one 클릭했을 때 RecipeWriteBothTitle 이동
        val one: TextView = findViewById(R.id.one)
        one.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothTitle::class.java)
            startActivity(intent)
        }

        // two 클릭했을 때 RecipeWriteBothMaterial 이동
        val two: TextView = findViewById(R.id.two)
        two.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothMaterial::class.java)
            startActivity(intent)
        }

        // three 클릭했을 때 RecipeWriteBothReplaceMaterial 이동
        val three: TextView = findViewById(R.id.three)
        three.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothReplaceMaterial::class.java)
            startActivity(intent)
        }

        // four 클릭했을 때 RecipeWriteBothHandlindMethod 이동
        val four: TextView = findViewById(R.id.four)
        four.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothHandlindMethod::class.java)
            startActivity(intent)
        }

        // five 클릭했을 때 RecipeWriteBothCookOrder 이동
        val five: TextView = findViewById(R.id.five)
        five.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothCookOrder::class.java)
            startActivity(intent)
        }

        // six 클릭했을 때 RecipeWriteBothCookVideo 이동
        val six: TextView = findViewById(R.id.six)
        six.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothCookVideo::class.java)
            startActivity(intent)
        }

        // 0(소요시간 입력)에 입력된 텍스트 가져오기
        val zero = findViewById<EditText>(R.id.zero)

        // 30(소요시간 입력)에 입력된 텍스트 가져오기
        val halfHour = findViewById<EditText>(R.id.halfHour)

        // 태그명을 입력해주세요에 입력된 텍스트 가져오기
        val recipeTitleWrite = findViewById<EditText>(R.id.recipeTitleWrite)

        // 드롭다운 요소 선언
        val dowArrowButton = findViewById<ImageButton>(R.id.dowArrow)
        val levelBox = findViewById<View>(R.id.levelBox)
        val beginningLevel = findViewById<TextView>(R.id.beginningLevel)
        val intermediateLevel = findViewById<TextView>(R.id.intermediateLevel)
        val upperLevel = findViewById<TextView>(R.id.upperLevel)
        val levelBoxBar = findViewById<View>(R.id.levelBoxBar)
        val levelBoxBarTwo = findViewById<View>(R.id.levelBoxBarTwo)
        val requiredTime = findViewById<TextView>(R.id.requiredTime)
        val elementaryLevel = findViewById<TextView>(R.id.elementaryLevel)

        // 드롭다운 버튼 클릭시
        dowArrowButton.setOnClickListener {
            // 난이도 선택 상자 보이기
            levelBox.visibility = View.VISIBLE
            levelBoxBar.visibility = View.VISIBLE
            levelBoxBarTwo.visibility = View.VISIBLE
            beginningLevel.visibility = View.VISIBLE
            intermediateLevel.visibility = View.VISIBLE
            upperLevel.visibility = View.VISIBLE

            // 소요시간 위치 변경
            val layoutParams = requiredTime.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.topToBottom = R.id.levelBox
            layoutParams.topMargin = 100 // 150dp 내려가도록 설정
            requiredTime.layoutParams = layoutParams
        }

        // 모든 난이도 TextView를 배열로 저장
        val levelTextViews = listOf(
            findViewById<TextView>(R.id.beginningLevel),
            findViewById<TextView>(R.id.intermediateLevel),
            findViewById<TextView>(R.id.upperLevel),
        )

        // 원래 topMargin 값 (초기값 설정)
        val originalTopMargin = 25 // XML에서 설정한 기본 마진 (dp 값 변환 필요)

        // dp를 px로 변환하는 함수
        fun dpToPx(dp: Int): Int {
            val density = resources.displayMetrics.density
            return (dp * density).toInt()
        }

        levelTextViews.forEach { textView ->
            textView.setOnClickListener {
                // 레시피 제목을 원래 위치로 되돌리기
                val layoutParams = requiredTime.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topMargin = dpToPx(originalTopMargin) // dp 값을 px로 변환해서 적용
                requiredTime.layoutParams = layoutParams

                // 모든 카테고리 뷰를 GONE으로 설정
                listOf(
                    levelBox, levelBoxBar, levelBoxBarTwo, beginningLevel, intermediateLevel,
                    upperLevel
                ).forEach { it.visibility = View.GONE }

                // 선택한 항목의 텍스트를 koreanFood TextView에 설정하고, 보이도록 변경
                elementaryLevel.text = textView.text
                elementaryLevel.visibility = View.VISIBLE
                elementaryLevel.setTextColor(Color.parseColor("#2B2B2B")) // 텍스트 색상 변경
            }
        }

        // 이전 화면으로 이동
        val backArrow: ImageButton = findViewById(R.id.backArrow)
        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }
}
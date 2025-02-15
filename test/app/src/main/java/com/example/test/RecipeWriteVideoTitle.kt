/*레시피 작성 동영상 STEP1*/
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

class RecipeWriteVideoTitle : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_video_title)

        // nextFixButton 클릭했을 때 RecipeWriteVideoMaterial 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoMaterial::class.java)
            startActivity(intent)
        }

        // two 클릭했을 때 RecipeWriteVideoMaterial 이동
        val two: TextView = findViewById(R.id.two)
        two.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoMaterial::class.java)
            startActivity(intent)
        }

        // three 클릭했을 때 RecipeWriteVideoReplaceMaterial 이동
        val three: TextView = findViewById(R.id.three)
        three.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoReplaceMaterial::class.java)
            startActivity(intent)
        }

        // four 클릭했을 때 RecipeWriteVideoHandlingMethod 이동
        val four: TextView = findViewById(R.id.four)
        four.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoHandlingMethod::class.java)
            startActivity(intent)
        }

        // five 클릭했을 때 RecipeWriteVideoCookVideo 이동
        val five: TextView = findViewById(R.id.five)
        five.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoCookVideo::class.java)
            startActivity(intent)
        }

        // six 클릭했을 때 RecipeWriteVideoDetailSettle 이동
        val six: TextView = findViewById(R.id.six)
        six.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoDetailSettle::class.java)
            startActivity(intent)
        }

        // 레시피 제목을 입력해주세요에 입력된 텍스트 가져오기
        val editText = findViewById<EditText>(R.id.recipeTitleWrite)

        // 드롭다운 요소 선언
        val dropDownButton = findViewById<ImageButton>(R.id.dowArrow)
        val categoryBox = findViewById<View>(R.id.categoryBox)
        val categoryBoxBar = findViewById<View>(R.id.categoryBoxBar)
        val categoryBoxBarTwo = findViewById<View>(R.id.categoryBoxBarTwo)
        val categoryBoxBarThree = findViewById<View>(R.id.categoryBoxBarThree)
        val categoryBoxBarFour = findViewById<View>(R.id.categoryBoxBarFour)
        val categoryBoxBarFive = findViewById<View>(R.id.categoryBoxBarFive)
        val categoryBoxBarSix = findViewById<View>(R.id.categoryBoxBarSix)
        val categoryBoxBarSeven = findViewById<View>(R.id.categoryBoxBarSeven)
        val categoryBoxBarEight = findViewById<View>(R.id.categoryBoxBarEight)
        val categoryBoxBarNine = findViewById<View>(R.id.categoryBoxBarNine)
        val recipeTitle = findViewById<TextView>(R.id.recipeTitle)
        val koreanFood = findViewById<TextView>(R.id.koreanFood)
        val total = findViewById<TextView>(R.id.total)
        val koreaFood = findViewById<TextView>(R.id.koreaFood)
        val westernFood = findViewById<TextView>(R.id.westernFood)
        val japaneseFood = findViewById<TextView>(R.id.japaneseFood)
        val chineseFood = findViewById<TextView>(R.id.chineseFood)
        val vegetarianDiet = findViewById<TextView>(R.id.vegetarianDiet)
        val snack = findViewById<TextView>(R.id.snack)
        val alcoholSnack = findViewById<TextView>(R.id.alcoholSnack)
        val sideDish = findViewById<TextView>(R.id.sideDish)
        val etc = findViewById<TextView>(R.id.etc)

        // 드롭다운 버튼 클릭시
        dropDownButton.setOnClickListener {
            // 드롭다운 요소 보이게 만들기
            categoryBox.visibility = View.VISIBLE
            total.visibility = View.VISIBLE
            categoryBoxBar.visibility = View.VISIBLE
            koreaFood.visibility = View.VISIBLE
            categoryBoxBarTwo.visibility = View.VISIBLE
            westernFood.visibility = View.VISIBLE
            categoryBoxBarThree.visibility = View.VISIBLE
            japaneseFood.visibility = View.VISIBLE
            categoryBoxBarFour.visibility = View.VISIBLE
            chineseFood.visibility = View.VISIBLE
            categoryBoxBarFive.visibility = View.VISIBLE
            vegetarianDiet.visibility = View.VISIBLE
            categoryBoxBarSix.visibility = View.VISIBLE
            snack.visibility = View.VISIBLE
            categoryBoxBarSeven.visibility = View.VISIBLE
            alcoholSnack.visibility = View.VISIBLE
            categoryBoxBarEight.visibility = View.VISIBLE
            sideDish.visibility = View.VISIBLE
            categoryBoxBarNine.visibility = View.VISIBLE
            etc.visibility = View.VISIBLE

            // 레시피 제목을 235dp 아래로 이동
            val layoutParams = recipeTitle.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.topMargin += 610 // 기존 위치에서 235dp 아래로 이동
            recipeTitle.layoutParams = layoutParams
        }

        // 모든 카테고리 TextView를 배열로 저장
        val categoryTextViews = listOf(
            findViewById<TextView>(R.id.total),
            findViewById<TextView>(R.id.koreaFood),
            findViewById<TextView>(R.id.westernFood),
            findViewById<TextView>(R.id.japaneseFood),
            findViewById<TextView>(R.id.chineseFood),
            findViewById<TextView>(R.id.vegetarianDiet),
            findViewById<TextView>(R.id.snack),
            findViewById<TextView>(R.id.alcoholSnack),
            findViewById<TextView>(R.id.sideDish),
            findViewById<TextView>(R.id.etc)
        )

        // 원래 topMargin 값 (초기값 설정)
        val originalTopMargin = 35 // XML에서 설정한 기본 마진 (dp 값 변환 필요)

        // dp를 px로 변환하는 함수
        fun dpToPx(dp: Int): Int {
            val density = resources.displayMetrics.density
            return (dp * density).toInt()
        }

        categoryTextViews.forEach { textView ->
            textView.setOnClickListener {
                // 레시피 제목을 원래 위치로 되돌리기
                val layoutParams = recipeTitle.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topMargin = dpToPx(originalTopMargin) // dp 값을 px로 변환해서 적용
                recipeTitle.layoutParams = layoutParams

                // 모든 카테고리 뷰를 GONE으로 설정
                listOf(
                    categoryBox, total, categoryBoxBar, koreaFood, categoryBoxBarTwo,
                    westernFood, categoryBoxBarThree, japaneseFood, categoryBoxBarFour,
                    chineseFood, categoryBoxBarFive, vegetarianDiet, categoryBoxBarSix,
                    snack, categoryBoxBarSeven, alcoholSnack, categoryBoxBarEight,
                    sideDish, categoryBoxBarNine, etc
                ).forEach { it.visibility = View.GONE }

                // 선택한 항목의 텍스트를 koreanFood TextView에 설정하고, 보이도록 변경
                koreanFood.text = textView.text
                koreanFood.visibility = View.VISIBLE
                koreanFood.setTextColor(Color.parseColor("#2B2B2B")) // 텍스트 색상 변경
            }
        }

        // 이전 화면으로 이동
        val backArrow: ImageButton = findViewById(R.id.backArrow)
        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }
}

/*냉장고 재료 관리 - 재료를 통한 레시피 추천*/
package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.widget.ImageButton
import androidx.core.view.children

class FridgeRecipeActivity : AppCompatActivity() {

    private var isChecked = false // 아이콘 상태 저장 변수

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge_recipe)

        // 카테고리 버튼들 (LinearLayout)
        val fridgeCategoryAllBtn: LinearLayout = findViewById(R.id.fridgeCategoryAllBtn)
        val fridgeCategoryColdBtn: LinearLayout = findViewById(R.id.fridgeCategoryColdBtn)
        val fridgeCategoryFreezeBtn: LinearLayout = findViewById(R.id.fridgeCategoryFreezeBtn)
        val fridgeCategoryOutBtn: LinearLayout = findViewById(R.id.fridgeCategoryOutBtn)

        // 카테고리 텍스트 (TextView)
        val fridgeCategoryAllText: TextView = findViewById(R.id.fridgeCategoryAllText)
        val fridgeCategoryColdText: TextView = findViewById(R.id.fridgeCategoryColdText)
        val fridgeCategoryFreezeText: TextView = findViewById(R.id.fridgeCategoryFreezeText)
        val fridgeCategoryOutText: TextView = findViewById(R.id.fridgeCategoryOutText)

        // 버튼 & 텍스트 리스트
        val categories = listOf(
            Pair(fridgeCategoryAllBtn, fridgeCategoryAllText),
            Pair(fridgeCategoryColdBtn, fridgeCategoryColdText),
            Pair(fridgeCategoryFreezeBtn, fridgeCategoryFreezeText),
            Pair(fridgeCategoryOutBtn, fridgeCategoryOutText)
        )

        // 카테고리 클릭 이벤트 설정
        categories.forEach { (layout, textView) ->
            layout.setOnClickListener {
                // 모든 버튼을 기본 색상으로 변경
                categories.forEach { (otherLayout, otherText) ->
                    otherLayout.setBackgroundResource(R.drawable.btn_fridge_ct) // 기본 배경
                    otherText.setTextColor(ContextCompat.getColor(this, R.color.black)) // 글자색 검정
                }

                // 선택된 버튼을 강조 색상으로 변경
                layout.setBackgroundResource(R.drawable.btn_fridge_ct_ck) // 선택된 배경
                textView.setTextColor(ContextCompat.getColor(this, R.color.white)) // 글자색 흰색
            }
        }

        // 전체선택 아이콘 버튼 설정
        val fridgeAllCheckIcon: ImageView = findViewById(R.id.fridgeAllCheckIcon)
        fridgeAllCheckIcon.setOnClickListener {
            isChecked = !isChecked // 상태 변경
            if (isChecked) {
                fridgeAllCheckIcon.setImageResource(R.drawable.btn_fridge_checked) // 체크된 상태
            } else {
                fridgeAllCheckIcon.setImageResource(R.drawable.ic_fridge_check) // 체크 해제 상태
            }
        }

        // backBtn 클릭했을 때 MaterialSalesActivity 이동
        val backBtn: ImageView = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // fridgeRecipe1 클릭했을 때 RecipeMainOne 이동
        val fridgeRecipe1: LinearLayout = findViewById(R.id.fridgeRecipe1)
        fridgeRecipe1.setOnClickListener {
            val intent = Intent(this, RecipeMainOne::class.java)
            startActivity(intent)
        }
    }
}
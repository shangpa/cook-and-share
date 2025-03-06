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
import androidx.core.view.children
import java.text.SimpleDateFormat
import java.util.*

class FridgeActivity : AppCompatActivity() {
    private var isChecked = false // 아이콘 상태 저장 변수

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)

        // 오늘 날짜를 yyyy.MM.dd 형식으로 가져오기
        val todayDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())

        // dayInput TextView에 오늘 날짜 설정
        val dayInput: TextView = findViewById(R.id.dayInput)
        dayInput.text = todayDate


        // rootLayout을 LinearLayout 혹은 ViewGroup으로 정의해야 합니다.
        val rootLayout: ViewGroup = findViewById(R.id.rootLayout) // rootLayout은 최상위 레이아웃의 ID

        // 모든 LinearLayout 찾아서 배경 변경
        val allFridgeLayouts = rootLayout.children.filterIsInstance<LinearLayout>()

        // 전체선택 아이콘 버튼 설정
        val fridgeAllCheckIcon: ImageView = findViewById(R.id.fridgeAllCheckIcon)
        fridgeAllCheckIcon.setOnClickListener {
            isChecked = !isChecked // 상태 변경
            if (isChecked) {
                fridgeAllCheckIcon.setImageResource(R.drawable.btn_fridge_checked) // 체크된 상태
                // 모든 LinearLayout 배경을 rounded_rectangle_fridge_ck로 변경
                allFridgeLayouts.forEach { layout ->
                    layout.setBackgroundResource(R.drawable.rounded_rectangle_fridge_ck)
                }
            } else {
                fridgeAllCheckIcon.setImageResource(R.drawable.ic_fridge_check) // 체크 해제 상태
                // 모든 LinearLayout 배경을 원래의 rounded_rectangle_fridge로 변경
                allFridgeLayouts.forEach { layout ->
                    layout.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                }
            }
        }


        // 카테고리 버튼들 (LinearLayout)
        val categoryAll: LinearLayout = findViewById(R.id.categoryAll)
        val categoryFridge: LinearLayout = findViewById(R.id.categoryFridge)
        val categoryFreeze: LinearLayout = findViewById(R.id.categoryFreeze)
        val categoryRoom: LinearLayout = findViewById(R.id.categoryRoom)

        // 카테고리 텍스트 (TextView)
        val textAll: TextView = findViewById(R.id.textAll)
        val textFridge: TextView = findViewById(R.id.textFridge)
        val textFreeze: TextView = findViewById(R.id.textFreeze)
        val textRoom: TextView = findViewById(R.id.textRoom)

        // 버튼 & 텍스트 리스트
        val categories = listOf(
            Pair(categoryAll, textAll),
            Pair(categoryFridge, textFridge),
            Pair(categoryFreeze, textFreeze),
            Pair(categoryRoom, textRoom)
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

        // fridgeAddBtn 클릭했을 때 FridgeMaterialActivity 이동
        val fridgeAddBtn: LinearLayout = findViewById(R.id.fridgeAddBtn)
        fridgeAddBtn.setOnClickListener {
            val intent = Intent(this, FridgeIngredientActivity::class.java)
            startActivity(intent)
        }

        // recipeRecommendBtn 클릭했을 때 FridgeRecipeActivity 이동
        val recipeRecommendBtn: LinearLayout = findViewById(R.id.recipeRecommendBtn)
        recipeRecommendBtn.setOnClickListener {
            val intent = Intent(this, FridgeRecipeActivity::class.java)
            startActivity(intent)
        }
    }
}

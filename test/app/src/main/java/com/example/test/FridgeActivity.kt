package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class FridgeActivity : AppCompatActivity() {
    private var isChecked = false // 아이콘 상태 저장 변수
    private val selectedLayouts = mutableSetOf<LinearLayout>() // 선택된 레이아웃 목록

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)

        val todayDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
        val dayInput: TextView = findViewById(R.id.dayInput)
        dayInput.text = todayDate

        val rootLayout: ViewGroup = findViewById(R.id.rootLayout)
        val allFridgeLayouts = rootLayout.children.filterIsInstance<LinearLayout>()

        val fridgeAllCheckIcon: ImageView = findViewById(R.id.fridgeAllCheckIcon)
        fridgeAllCheckIcon.setOnClickListener {
            isChecked = !isChecked
            if (isChecked) {
                fridgeAllCheckIcon.setImageResource(R.drawable.btn_fridge_checked)
                allFridgeLayouts.forEach { layout ->
                    layout.setBackgroundResource(R.drawable.rounded_rectangle_fridge_ck)
                    selectedLayouts.add(layout)
                }
            } else {
                fridgeAllCheckIcon.setImageResource(R.drawable.ic_fridge_check)
                allFridgeLayouts.forEach { layout ->
                    layout.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                    selectedLayouts.remove(layout)
                }
            }
        }

        allFridgeLayouts.forEach { layout ->
            layout.setOnClickListener {
                if (selectedLayouts.contains(layout)) {
                    layout.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                    selectedLayouts.remove(layout)
                } else {
                    layout.setBackgroundResource(R.drawable.rounded_rectangle_fridge_ck)
                    selectedLayouts.add(layout)
                }
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
        // 다른 화면의 데이터를 가져오는 부분
        val ingredient = intent.getStringExtra("ingredient") ?: ""
        val quantity = intent.getStringExtra("quantity") ?: ""
        val unit = intent.getStringExtra("unit") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val price = intent.getStringExtra("price") ?: ""

        // 입력 값 출력 (디버깅 용도)
        println("Ingredient: $ingredient, Quantity: $quantity, Unit: $unit, Date: $date, Price: $price")

        // 동적으로 추가될 재료 박스를 생성하는 함수 (예시)
        fun createIngredientBox(ingredientName: String, storageArea: String, expirationDate: String, ingredientNum: String): LinearLayout {
            val ingredientBox = LinearLayout(this)
            ingredientBox.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            ingredientBox.orientation = LinearLayout.HORIZONTAL
            ingredientBox.setPadding(15, 15, 15, 15)

            // 재료 이름 TextView
            val ingredientNameText = TextView(this)
            ingredientNameText.text = ingredientName
            ingredientNameText.textSize = 14f
            ingredientNameText.setTextColor(resources.getColor(R.color.green))

            // 보관장소 TextView
            val storageAreaText = TextView(this)
            storageAreaText.text = "보관장소 : $storageArea"
            storageAreaText.textSize = 11f
            storageAreaText.setTextColor(resources.getColor(R.color.black))

            // 유통기한 TextView
            val expirationDateText = TextView(this)
            expirationDateText.text = "유통기한 : $expirationDate"
            expirationDateText.textSize = 11f
            expirationDateText.setTextColor(resources.getColor(R.color.black))

            // 재료 갯수 TextView
            val ingredientNumText = TextView(this)
            ingredientNumText.text = "$ingredientNum 개"
            ingredientNumText.textSize = 14f
            ingredientNumText.setTextColor(resources.getColor(R.color.black))

            // 재료 박스에 텍스트 추가
            ingredientBox.addView(ingredientNameText)
            ingredientBox.addView(storageAreaText)
            ingredientBox.addView(expirationDateText)
            ingredientBox.addView(ingredientNumText)

            return ingredientBox
        }
    }
}

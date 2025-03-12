package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import java.text.SimpleDateFormat
import java.util.*

class FridgeActivity : AppCompatActivity() {
    private var isChecked = false // 아이콘 상태 저장 변수
    private val selectedLayouts = mutableSetOf<LinearLayout>() // 선택된 레이아웃 목록

    // dp -> px 변환 함수
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)

        val todayDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
        val dayInput: TextView = findViewById(R.id.dayInput)
        dayInput.text = todayDate

        // 카테고리 컨테이너 참조 및 초기 선택 (전체)
        val categoryAll: LinearLayout = findViewById(R.id.categoryAll)
        val categoryFridge: LinearLayout = findViewById(R.id.categoryFridge)
        val categoryFreeze: LinearLayout = findViewById(R.id.categoryFreeze)
        val categoryRoom: LinearLayout = findViewById(R.id.categoryRoom)
        val categoryViews = listOf(categoryAll, categoryFridge, categoryFreeze, categoryRoom)
        setCategorySelected(categoryAll, categoryViews)
        categoryViews.forEach { container ->
            container.setOnClickListener { setCategorySelected(it as LinearLayout, categoryViews) }
        }

        val rootLayout: LinearLayout = findViewById(R.id.rootLayout)
        val fridgeAllCheckIcon: ImageView = findViewById(R.id.fridgeAllCheckIcon)
        fridgeAllCheckIcon.setOnClickListener {
            isChecked = !isChecked
            fridgeAllCheckIcon.setImageResource(
                if (isChecked) R.drawable.btn_fridge_checked else R.drawable.ic_fridge_check
            )
            rootLayout.children.filterIsInstance<LinearLayout>().forEach { layout ->
                layout.setBackgroundResource(
                    if (isChecked) R.drawable.rounded_rectangle_fridge_ck
                    else R.drawable.rounded_rectangle_fridge
                )
                if (isChecked) selectedLayouts.add(layout) else selectedLayouts.remove(layout)
            }
        }

        // 기존 재료 레이아웃 클릭 리스너 등록
        rootLayout.children.filterIsInstance<LinearLayout>().forEach { layout ->
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

        // 재료 추가 버튼 → FridgeIngredientActivity 이동 (새로 추가)
        val fridgeAddBtn: LinearLayout = findViewById(R.id.fridgeAddBtn)
        fridgeAddBtn.setOnClickListener {
            val intent = Intent(this, FridgeIngredientActivity::class.java)
            startActivity(intent)
        }

        // 레시피 추천 버튼 → FridgeRecipeActivity 이동
        val recipeRecommendBtn: LinearLayout = findViewById(R.id.recipeRecommendBtn)
        recipeRecommendBtn.setOnClickListener {
            val intent = Intent(this, FridgeRecipeActivity::class.java)
            startActivity(intent)
        }

        // 삭제 버튼 클릭 시 선택된 재료 삭제
        val fridgeDeleteText: TextView = findViewById(R.id.fridgeDeleteText)
        fridgeDeleteText.setOnClickListener {
            selectedLayouts.forEach { rootLayout.removeView(it) }
            selectedLayouts.clear()
        }

        // 편집 버튼 (fridgeEditText) 클릭 시
        val fridgeEditText: TextView = findViewById(R.id.fridgeEditText)
        fridgeEditText.setOnClickListener {
            if (selectedLayouts.size != 1) {
                Toast.makeText(this, "편집할 항목을 하나 선택해 주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val selectedBox = selectedLayouts.first()
                val row1 = selectedBox.getChildAt(0) as? LinearLayout
                val row2 = selectedBox.getChildAt(1) as? LinearLayout
                val row3 = selectedBox.getChildAt(2) as? LinearLayout

                if (row1 != null && row2 != null && row3 != null) {
                    val ingredientName = (row1.getChildAt(0) as? TextView)?.text.toString()
                    val quantityAndUnit = (row1.getChildAt(1) as? TextView)?.text.toString()
                    val parts = quantityAndUnit.split(" ")
                    val quantity = if (parts.isNotEmpty()) parts[0] else ""
                    val unit = if (parts.size > 1) parts[1] else ""
                    val storageArea = (row2.getChildAt(1) as? TextView)?.text.toString()
                    // row3: 첫 번째 TextView는 dateOption label, 두 번째는 dateValue
                    val dateOption = (row3.getChildAt(0) as? TextView)?.text.toString().removeSuffix(" : ")
                    val dateValue = (row3.getChildAt(1) as? TextView)?.text.toString()

                    val intent = Intent(this, FridgeIngredientActivity::class.java)
                    intent.putExtra("ingredientName", ingredientName)
                    intent.putExtra("quantity", quantity)
                    intent.putExtra("unit", unit)
                    intent.putExtra("storageArea", storageArea)
                    intent.putExtra("fridgeDate", dateValue)
                    intent.putExtra("dateOption", dateOption)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "선택된 항목의 데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 인텐트로 전달된 데이터 가져오기 (편집 후 수정한 경우 또는 새로 추가된 데이터)
        val intentIngredientName = intent.getStringExtra("ingredientName") ?: ""
        val storageAreaExtra = intent.getStringExtra("storageArea") ?: ""
        val fridgeDateExtra = intent.getStringExtra("fridgeDate") ?: ""
        val dateOptionExtra = intent.getStringExtra("dateOption") ?: "유통기한" // 기본값
        val quantityExtra = intent.getStringExtra("quantity") ?: ""
        val unitExtra = intent.getStringExtra("unit") ?: ""

        if (intentIngredientName.isNotEmpty()) {
            val existingView = rootLayout.children.find { view ->
                view is LinearLayout && view.tag == intentIngredientName
            }
            if (existingView != null) {
                // 업데이트: 기존 항목의 내용을 수정
                val ingredientBox = existingView as LinearLayout
                val row1 = ingredientBox.getChildAt(0) as? LinearLayout
                val row2 = ingredientBox.getChildAt(1) as? LinearLayout
                val row3 = ingredientBox.getChildAt(2) as? LinearLayout
                row1?.let {
                    (it.getChildAt(0) as? TextView)?.text = intentIngredientName
                    (it.getChildAt(1) as? TextView)?.text = "$quantityExtra $unitExtra"
                }
                row2?.let {
                    (it.getChildAt(1) as? TextView)?.text = storageAreaExtra
                }
                row3?.let {
                    (it.getChildAt(0) as? TextView)?.text = "$dateOptionExtra : "
                    (it.getChildAt(1) as? TextView)?.text = fridgeDateExtra
                }
            } else {
                // 새 항목 추가
                val ingredientBox = createIngredientBox(
                    ingredientName = intentIngredientName,
                    storageArea = storageAreaExtra,
                    dateOption = dateOptionExtra,
                    dateValue = fridgeDateExtra,
                    quantity = quantityExtra,
                    unit = unitExtra
                )
                ingredientBox.setOnClickListener {
                    if (selectedLayouts.contains(ingredientBox)) {
                        ingredientBox.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                        selectedLayouts.remove(ingredientBox)
                    } else {
                        ingredientBox.setBackgroundResource(R.drawable.rounded_rectangle_fridge_ck)
                        selectedLayouts.add(ingredientBox)
                    }
                }
                ingredientBox.tag = intentIngredientName
                rootLayout.addView(ingredientBox)
            }
        }
    }

    private fun setCategorySelected(selected: LinearLayout, allCategories: List<LinearLayout>) {
        for (container in allCategories) {
            val textView = container.getChildAt(0) as? TextView
            if (container == selected) {
                container.setBackgroundResource(R.drawable.btn_fridge_ct_ck)
                textView?.setTextColor(android.graphics.Color.WHITE)
            } else {
                container.setBackgroundResource(R.drawable.btn_fridge_ct)
                textView?.setTextColor(android.graphics.Color.BLACK)
            }
        }
    }

    // 재료 박스를 생성하는 함수
    // row3는 드롭다운에서 선택한 날짜 옵션과 입력한 값을 "dateOption : dateValue" 형식으로 출력합니다.
    private fun createIngredientBox(
        ingredientName: String,
        storageArea: String,
        dateOption: String,
        dateValue: String,
        quantity: String,
        unit: String
    ): LinearLayout {
        val marginLR = dpToPx(30)
        val marginBottom = dpToPx(10)
        val paddingLR = dpToPx(30)
        val paddingTB = dpToPx(15)

        val ingredientBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.rounded_rectangle_fridge)
            setPadding(paddingLR, paddingTB, paddingLR, paddingTB)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(marginLR, 0, marginLR, marginBottom)
            }
        }

        // row1: 재료명 및 수량/단위
        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val ingredientNameText = TextView(this).apply {
            text = ingredientName
            textSize = 16f
            setTextColor(resources.getColor(R.color.green))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val quantityText = TextView(this).apply {
            text = "$quantity $unit"
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(resources.getColor(R.color.black))
        }
        row1.addView(ingredientNameText)
        row1.addView(quantityText)

        // row2: 보관장소
        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val storageAreaLabel = TextView(this).apply {
            text = "보관장소 : "
            textSize = 12f
            setTextColor(resources.getColor(R.color.black))
        }
        val storageAreaText = TextView(this).apply {
            text = storageArea
            textSize = 12f
            setTextColor(resources.getColor(R.color.black))
        }
        row2.addView(storageAreaLabel)
        row2.addView(storageAreaText)

        // row3: 날짜 옵션과 입력값 출력 (예: "유통기한 : 2025.01.18")
        val row3 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val dateOptionLabel = TextView(this).apply {
            text = "$dateOption : "
            textSize = 12f
            setTextColor(resources.getColor(R.color.black))
        }
        val dateValueText = TextView(this).apply {
            text = dateValue
            textSize = 12f
            setTextColor(resources.getColor(R.color.black))
        }
        row3.addView(dateOptionLabel)
        row3.addView(dateValueText)

        ingredientBox.addView(row1)
        ingredientBox.addView(row2)
        ingredientBox.addView(row3)

        return ingredientBox
    }
}

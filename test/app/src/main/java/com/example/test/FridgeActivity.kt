package com.example.test

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.test.model.FridgeResponse
import com.example.test.network.ApiService
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import androidx.core.content.ContextCompat
import java.util.*

class FridgeActivity : AppCompatActivity() {
    private var isChecked = false // 아이콘 상태 저장 변수
    private val selectedLayouts = mutableSetOf<LinearLayout>() // 선택된 레이아웃 목록

    // Retrofit API 서비스
    private lateinit var apiService: ApiService

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)

        // Retrofit API 서비스 초기화
        apiService = RetrofitInstance.apiService

        val todayDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
        val dayInput: TextView = findViewById(R.id.dayInput)
        dayInput.text = todayDate

        // 카테고리 컨테이너 및 초기 선택
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
                    if (isChecked) R.drawable.rounded_rectangle_fridge_ck else R.drawable.rounded_rectangle_fridge
                )
                if (isChecked) selectedLayouts.add(layout) else selectedLayouts.remove(layout)
            }
        }

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

        val fridgeAddBtn: LinearLayout = findViewById(R.id.fridgeAddBtn)
        fridgeAddBtn.setOnClickListener {
            startActivity(Intent(this, FridgeIngredientActivity::class.java))
        }

        val recipeRecommendBtn: LinearLayout = findViewById(R.id.recipeRecommendBtn)
        recipeRecommendBtn.setOnClickListener {
            startActivity(Intent(this, FridgeRecipeActivity::class.java))
        }

        val fridgeDeleteText: TextView = findViewById(R.id.fridgeDeleteText)
        fridgeDeleteText.setOnClickListener {
            selectedLayouts.forEach { rootLayout.removeView(it) }
            selectedLayouts.clear()
        }

        val fridgeEditText: TextView = findViewById(R.id.fridgeEditText)
        fridgeEditText.setOnClickListener {
            if (selectedLayouts.size != 1) {
                Toast.makeText(this, "편집할 항목을 하나 선택해 주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val selectedBox = selectedLayouts.first()
                // 기존 편집 로직 그대로 사용 (수정 시 FridgeIngredientActivity로 이동)
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
    }

    // onResume에서 백엔드에서 최신 냉장고 데이터 불러오기
    override fun onResume() {
        super.onResume()
        fetchFridgeData()
    }

    // 백엔드 API 호출하여 냉장고 데이터 불러오기 (JWT 토큰 포함)
    private fun fetchFridgeData() {
        val rootLayout: LinearLayout = findViewById(R.id.rootLayout)
        // 기존 뷰에 추가된 항목들을 모두 삭제
        rootLayout.removeAllViews()
        // 로그인한 사용자의 ID와 JWT 토큰 가져오기
        val userId = getLoggedInUserId()
        val token = "Bearer " + getTokenFromPreferences()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getMyFridges(token, userId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val fridgeList = response.body() ?: listOf()
                        fridgeList.forEach { fridgeResponse ->
                            // FridgeResponse 데이터를 사용하여 뷰 생성
                            val ingredientBox = createIngredientBox(
                                ingredientName = fridgeResponse.ingredientName,
                                storageArea = fridgeResponse.storageArea,
                                dateOption = fridgeResponse.dateOption ?: "유통기한",
                                dateValue = fridgeResponse.fridgeDate,
                                quantity = fridgeResponse.quantity.toString(),
                                unit = fridgeResponse.unitDetail
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
                            ingredientBox.tag = fridgeResponse.ingredientName
                            rootLayout.addView(ingredientBox)
                        }
                    } else {
                        Toast.makeText(
                            this@FridgeActivity,
                            "데이터 불러오기 실패: ${response.errorBody()?.string()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@FridgeActivity,
                        "네트워크 오류: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
            setTextColor(resources.getColor(R.color.black, theme))
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
            setTextColor(resources.getColor(R.color.black, theme))
        }
        val storageAreaText = TextView(this).apply {
            text = storageArea
            textSize = 12f
            setTextColor(resources.getColor(R.color.black, theme))
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
            setTextColor(resources.getColor(R.color.black, theme))
        }
        val dateValueText = TextView(this).apply {
            text = dateValue
            textSize = 12f
            setTextColor(resources.getColor(R.color.black, theme))
        }
        row3.addView(dateOptionLabel)
        row3.addView(dateValueText)

        ingredientBox.addView(row1)
        ingredientBox.addView(row2)
        ingredientBox.addView(row3)

        return ingredientBox
    }

    // 예시: 로그인한 사용자 ID를 반환하는 메서드 (실제 구현에서는 인증 컨텍스트나 SharedPreferences에서 가져옴)
    private fun getLoggedInUserId(): Long {
        return 1L
    }

    // 예시: 실제 저장된 JWT 토큰을 반환하는 메서드 (실제 구현에서는 SharedPreferences 등 사용)
    private fun getTokenFromPreferences(): String {
        // 여기에 실제 토큰을 반환하도록 구현하세요.
        return "your_actual_jwt_token"
    }
}

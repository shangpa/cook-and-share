package com.example.test

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.test.model.FridgeResponse
import com.example.test.network.ApiService
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class FridgeActivity : AppCompatActivity() {

    private var isChecked = false
    private val selectedLayouts = mutableSetOf<LinearLayout>()
    private var allFridgeList: List<FridgeResponse> = listOf()
    private var currentCategory = "전체"

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)

        apiService = RetrofitInstance.apiService

        val todayDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
        findViewById<TextView>(R.id.dayInput).text = todayDate

        val categoryAll: LinearLayout = findViewById(R.id.categoryAll)
        val categoryFridge: LinearLayout = findViewById(R.id.categoryFridge)
        val categoryFreeze: LinearLayout = findViewById(R.id.categoryFreeze)
        val categoryRoom: LinearLayout = findViewById(R.id.categoryRoom)
        val categoryViews = listOf(categoryAll, categoryFridge, categoryFreeze, categoryRoom)

        setCategorySelected(categoryAll, categoryViews)
        categoryViews.forEach { container ->
            container.setOnClickListener {
                setCategorySelected(it as LinearLayout, categoryViews)
            }
        }

        findViewById<ImageView>(R.id.fridgeAllCheckIcon).setOnClickListener {
            isChecked = !isChecked
            it as ImageView
            it.setImageResource(
                if (isChecked) R.drawable.btn_fridge_checked else R.drawable.ic_fridge_check
            )
            val rootLayout: LinearLayout = findViewById(R.id.rootLayout)
            rootLayout.children.filterIsInstance<LinearLayout>().forEach { layout ->
                layout.setBackgroundResource(
                    if (isChecked) R.drawable.rounded_rectangle_fridge_ck else R.drawable.rounded_rectangle_fridge
                )
                if (isChecked) selectedLayouts.add(layout) else selectedLayouts.remove(layout)
            }
        }

        findViewById<LinearLayout>(R.id.fridgeAddBtn).setOnClickListener {
            startActivity(Intent(this, FridgeIngredientActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.recipeRecommendBtn).setOnClickListener {
            val selectedIngredients = selectedLayouts.map { layout ->
                val row1 = layout.getChildAt(0) as? LinearLayout
                (row1?.getChildAt(0) as? TextView)?.text.toString()
            }
            Log.d("선택한재료", "선택한 재료 리스트: $selectedIngredients")

            val intent = Intent(this, FridgeRecipeActivity::class.java).apply {
                putStringArrayListExtra("selectedIngredients", ArrayList(selectedIngredients))
            }
            if (selectedIngredients.isEmpty()) {
                Toast.makeText(this, "추천할 재료를 선택해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(intent)
        }

        findViewById<TextView>(R.id.fridgeDeleteText).setOnClickListener {
            if (selectedLayouts.isEmpty()) {
                Toast.makeText(this, "삭제할 항목을 선택해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = "Bearer ${getTokenFromPreferences()}"
            val rootLayout: LinearLayout = findViewById(R.id.rootLayout)

            selectedLayouts.forEach { layout ->
                val row1 = layout.getChildAt(0) as? LinearLayout
                val row2 = layout.getChildAt(1) as? LinearLayout
                val row3 = layout.getChildAt(2) as? LinearLayout

                if (row1 != null && row2 != null && row3 != null) {
                    val ingredientName = (row1.getChildAt(0) as? TextView)?.text.toString()
                    val quantityAndUnit = (row1.getChildAt(1) as? TextView)?.text.toString()
                    val parts = quantityAndUnit.split(" ")
                    val quantity = if (parts.isNotEmpty()) parts[0] else ""
                    val unit = if (parts.size > 1) parts[1] else ""
                    val storageArea = (row2.getChildAt(1) as? TextView)?.text.toString()
                    val dateValue = (row3.getChildAt(1) as? TextView)?.text.toString()

                    val quantityNum = quantity.toDoubleOrNull()

                    val targetFridge = allFridgeList.find {
                        it.ingredientName == ingredientName &&
                                it.storageArea == storageArea &&
                                it.fridgeDate == dateValue &&
                                it.unitDetail == unit &&
                                it.quantity == quantityNum
                    }

                    if (targetFridge != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = apiService.deleteFridge(targetFridge.id, token)
                                withContext(Dispatchers.Main) {
                                    if (response.isSuccessful) {
                                        rootLayout.removeView(layout)
                                        selectedLayouts.remove(layout)
                                        Toast.makeText(this@FridgeActivity, "삭제 완료!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@FridgeActivity, "삭제 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@FridgeActivity, "삭제 중 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "선택된 항목의 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


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
                    val dateOption = (row3.getChildAt(0) as? TextView)?.text.toString().removeSuffix(" : ")
                    val dateValue = (row3.getChildAt(1) as? TextView)?.text.toString()

                    //  fridgeId를 정확하게 찾기 위해 조건을 보완
                    val quantityNum = quantity.toDoubleOrNull()
                    val targetFridge = allFridgeList.find {
                        it.ingredientName == ingredientName &&
                                it.storageArea == storageArea &&
                                it.fridgeDate == dateValue &&
                                it.unitDetail == unit &&
                                it.quantity == quantityNum
                    }


                    if (targetFridge == null) {
                        Log.e("FridgeActivity", "fridgeId를 찾을 수 없습니다: $ingredientName, $quantity, $unit, $storageArea, $dateValue")
                        Toast.makeText(this, "선택된 항목의 ID를 찾지 못했습니다.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val intent = Intent(this, FridgeIngredientActivity::class.java).apply {
                        putExtra("ingredientName", ingredientName)
                        putExtra("quantity", quantity)
                        putExtra("unit", unit)
                        putExtra("storageArea", storageArea)
                        putExtra("fridgeDate", dateValue)
                        putExtra("dateOption", dateOption)
                        putExtra("fridgeId", targetFridge.id)
                    }
                    Log.d("FridgeActivity", "선택된 fridgeId: ${targetFridge.id}")
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "선택된 항목의 데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val fridgeSearchInput = findViewById<EditText>(R.id.fridgeSearchInput)
        fridgeSearchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().trim()
                filterAndDisplayFridgeItems(currentCategory, keyword)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onResume() {
        super.onResume()
        fetchFridgeData()
    }

    private fun fetchFridgeData() {
        val token = "Bearer ${getTokenFromPreferences()}"
        if (token.isBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getMyFridges(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        allFridgeList = response.body()?.sortedByDescending { it.updatedAt } ?: listOf()
                        filterAndDisplayFridgeItems(currentCategory)
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
                    Toast.makeText(this@FridgeActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun filterAndDisplayFridgeItems(category: String, keyword: String = "") {
        val rootLayout: LinearLayout = findViewById(R.id.rootLayout)
        rootLayout.removeAllViews()
        selectedLayouts.clear()

        val filteredList = allFridgeList.filter {
            val categoryMatch = when (category) {
                "냉장" -> it.storageArea == "냉장"
                "냉동" -> it.storageArea == "냉동"
                "실온" -> it.storageArea == "실온"
                else -> true
            }
            val keywordMatch = it.ingredientName.contains(keyword, ignoreCase = true)
            categoryMatch && keywordMatch
        }

        filteredList.forEach { fridge ->
            val box = createIngredientBox(
                ingredientName = fridge.ingredientName,
                storageArea = fridge.storageArea,
                dateOption = fridge.dateOption ?: "유통기한",
                dateValue = fridge.fridgeDate,
                quantity = fridge.quantity.toString(),
                unit = fridge.unitDetail
            )
            box.setOnClickListener {
                if (selectedLayouts.contains(box)) {
                    box.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                    selectedLayouts.remove(box)
                } else {
                    box.setBackgroundResource(R.drawable.rounded_rectangle_fridge_ck)
                    selectedLayouts.add(box)
                }
            }
            rootLayout.addView(box)
        }
    }

    private fun setCategorySelected(selected: LinearLayout, allCategories: List<LinearLayout>) {
        val textView = selected.getChildAt(0) as? TextView
        currentCategory = textView?.text.toString()
        filterAndDisplayFridgeItems(currentCategory)

        for (container in allCategories) {
            val tv = container.getChildAt(0) as? TextView
            if (container == selected) {
                container.setBackgroundResource(R.drawable.btn_fridge_ct_ck)
                tv?.setTextColor(android.graphics.Color.WHITE)
            } else {
                container.setBackgroundResource(R.drawable.btn_fridge_ct)
                tv?.setTextColor(android.graphics.Color.BLACK)
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

        val quantityDisplay = if (quantity.toDoubleOrNull()?.rem(1.0) == 0.0) {
            quantity.toDouble().toInt().toString()
        } else {
            quantity
        }

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.rounded_rectangle_fridge)
            setPadding(paddingLR, paddingTB, paddingLR, paddingTB)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(marginLR, 0, marginLR, marginBottom)
            }

            val row1 = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            val nameText = TextView(context).apply {
                text = ingredientName
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                textSize = 16f
                setTextColor(resources.getColor(R.color.green))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val qtyText = TextView(context).apply {
                text = "$quantityDisplay $unit"
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                textSize = 16f
                setTextColor(resources.getColor(R.color.black))
            }
            row1.addView(nameText)
            row1.addView(qtyText)

            val row2 = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            row2.addView(TextView(context).apply {
                text = "보관장소 : "
                textSize = 12f
            })
            row2.addView(TextView(context).apply {
                text = storageArea
                textSize = 12f
            })

            val row3 = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            row3.addView(TextView(context).apply {
                text = "$dateOption : "
                textSize = 12f
            })
            row3.addView(TextView(context).apply {
                text = dateValue
                textSize = 12f
            })

            addView(row1)
            addView(row2)
            addView(row3)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun getTokenFromPreferences(): String {
        return App.prefs.token ?: ""
    }
}

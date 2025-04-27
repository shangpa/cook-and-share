package com.example.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.test.model.FridgeRequest
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.*
import java.util.*

class FridgeIngredientActivity : AppCompatActivity() {

    private var selectedUnitCategory: String = "무게"
    private lateinit var apiService: com.example.test.network.ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge_ingredient)

        apiService = RetrofitInstance.apiService

        val fridgeStorageText: TextView = findViewById(R.id.fridgeStorageText)
        val fridgeStorageDropBtn: ImageView = findViewById(R.id.fridgeStorageDropBtn)
        val fridgeIngredientUnit: TextView = findViewById(R.id.fridgeIngredientUnit)
        val fridgeIngredientUnit2: TextView = findViewById(R.id.fridgeIngredientUnit2)
        val fridgeUnitDropBtn: ImageView = findViewById(R.id.fridgeUnitDropBtn)
        val fridgeUnitDropBtn2: ImageView = findViewById(R.id.fridgeUnitDropBtn2)
        val fridgeDateInput: EditText = findViewById(R.id.fridgeDateInput)
        val fridgeDateDropText: TextView = findViewById(R.id.fridgeDate)
        val dateDropBtn: ImageView = findViewById(R.id.DateDropBtn)
        val fridgeIngredientInput: EditText = findViewById(R.id.fridgeIngredientInput)
        val fridgeIngredientNumInput: EditText = findViewById(R.id.fridgeIngredientNumInput)
        val fridgeAddButton: LinearLayout = findViewById(R.id.fridgeAddButton)
        val backBtn: ImageView = findViewById(R.id.backBtn)

        // 드롭다운 설정
        fridgeStorageDropBtn.setOnClickListener {
            showPopupMenu(fridgeStorageDropBtn, fridgeStorageText, R.menu.fridge_storage_menu)
        }
        dateDropBtn.setOnClickListener {
            showPopupMenu(dateDropBtn, fridgeDateDropText, R.menu.fridge_date_menu)
        }
        fridgeUnitDropBtn2.setOnClickListener {
            val popup = PopupMenu(this, fridgeUnitDropBtn2)
            popup.menu.add("무게")
            popup.menu.add("부피")
            popup.menu.add("개수")
            popup.setOnMenuItemClickListener { item ->
                selectedUnitCategory = item.title.toString()
                fridgeIngredientUnit2.text = selectedUnitCategory
                fridgeIngredientUnit.text = "선택"
                true
            }
            popup.show()
        }
        fridgeUnitDropBtn.setOnClickListener {
            val popup = PopupMenu(this, fridgeUnitDropBtn)
            when (selectedUnitCategory) {
                "무게" -> listOf("kg", "g", "근", "푼")
                "부피" -> listOf("mL", "L", "스푼", "컵")
                "개수" -> listOf("알", "봉지", "포기", "개", "통", "장", "마리")
                else -> listOf("선택")
            }.forEach { popup.menu.add(it) }

            popup.setOnMenuItemClickListener { item ->
                fridgeIngredientUnit.text = item.title.toString()
                true
            }
            popup.show()
        }

        // 수정 데이터 intent로 받기
        val intentFridgeId = intent.getLongExtra("fridgeId", -1L)
        val intentIngredientName = intent.getStringExtra("ingredientName") ?: ""
        val intentQuantity = intent.getStringExtra("quantity") ?: ""
        val intentUnit = intent.getStringExtra("unit") ?: ""
        val intentStorageArea = intent.getStringExtra("storageArea") ?: ""
        val intentFridgeDate = intent.getStringExtra("fridgeDate") ?: ""
        val intentDateOption = intent.getStringExtra("dateOption") ?: ""

        if (intentIngredientName.isNotEmpty()) fridgeIngredientInput.setText(intentIngredientName)
        if (intentQuantity.isNotEmpty()) fridgeIngredientNumInput.setText(intentQuantity)
        if (intentUnit.isNotEmpty()) fridgeIngredientUnit.text = intentUnit
        if (intentStorageArea.isNotEmpty()) fridgeStorageText.text = intentStorageArea
        if (intentFridgeDate.isNotEmpty()) fridgeDateInput.setText(intentFridgeDate)
        if (intentDateOption.isNotEmpty()) {
            fridgeDateDropText.text = intentDateOption
            selectedUnitCategory = intentDateOption
            fridgeIngredientUnit2.text = intentDateOption
        }

        fridgeAddButton.setOnClickListener {
            val ingredientName = fridgeIngredientInput.text.toString().trim()
            val storageArea = fridgeStorageText.text.toString().trim()
            val fridgeDate = fridgeDateInput.text.toString().trim()
            val dateOption = fridgeDateDropText.text.toString().trim()
            val quantity = fridgeIngredientNumInput.text.toString().toDoubleOrNull() ?: 0.0

            val unitCategoryMap = mapOf(
                "무게" to "WEIGHT",
                "부피" to "VOLUME",
                "개수" to "COUNT"
            )

            val unitCategoryStr = fridgeIngredientUnit2.text.toString().trim()
            val unitCategory = unitCategoryMap[unitCategoryStr]

            if (unitCategory == null) {
                Toast.makeText(this, "단위 종류를 선택해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val unitDetail = fridgeIngredientUnit.text.toString().trim()
            if (unitDetail == "선택" || unitDetail.isEmpty()) {
                Toast.makeText(this, "단위를 선택해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = "Bearer ${getTokenFromPreferences()}"
            val userId = getLoggedInUserId()

            val fridgeRequest = FridgeRequest(
                ingredientName = ingredientName,
                storageArea = storageArea,
                fridgeDate = fridgeDate,
                dateOption = dateOption,
                quantity = quantity,
                unitCategory = unitCategory,
                unitDetail = unitDetail,
                userId = userId
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = if (intentFridgeId != -1L) {
                        apiService.updateFridge(intentFridgeId, token, fridgeRequest)
                    } else {
                        apiService.createFridge(token, fridgeRequest)
                    }

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            startActivity(Intent(this@FridgeIngredientActivity, FridgeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@FridgeIngredientActivity, "저장 실패: HTTP ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FridgeIngredientActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        backBtn.setOnClickListener {
            startActivity(Intent(this, FridgeActivity::class.java))
        }

        Log.d("FridgeIngredientActivity", "수정용 fridgeId: $intentFridgeId")

    }

    private fun showPopupMenu(anchor: ImageView, textView: TextView, menuRes: Int) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(menuRes, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            textView.text = it.title
            true
        }
        popupMenu.show()
    }

    private fun getLoggedInUserId(): Long {
        return App.prefs.userId
    }

    private fun getTokenFromPreferences(): String {
        return App.prefs.token ?: ""
    }

}

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
import com.example.test.model.FridgeRequest
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class FridgeIngredientActivity : AppCompatActivity() {

    // 현재 선택된 단위 범주, 기본값 "무게"
    private var selectedUnitCategory: String = "무게"

    // Retrofit API 서비스 인스턴스 (클래스 프로퍼티)
    private lateinit var apiService: com.example.test.network.ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.test.R.layout.activity_fridge_ingredient)

        // Retrofit API 서비스 초기화
        apiService = RetrofitInstance.apiService

        // 뷰 참조
        val fridgeStorageText: TextView = findViewById(com.example.test.R.id.fridgeStorageText)
        val fridgeStorageDropBtn: ImageView = findViewById(com.example.test.R.id.fridgeStorageDropBtn)
        val fridgeIngredientUnit: TextView = findViewById(com.example.test.R.id.fridgeIngredientUnit)
        val fridgeIngredientUnit2: TextView = findViewById(com.example.test.R.id.fridgeIngredientUnit2)
        val fridgeUnitDropBtn: ImageView = findViewById(com.example.test.R.id.fridgeUnitDropBtn)
        val fridgeUnitDropBtn2: ImageView = findViewById(com.example.test.R.id.fridgeUnitDropBtn2)
        val fridgeDateInput: EditText = findViewById(com.example.test.R.id.fridgeDateInput)
        val fridgeDateDropText: TextView = findViewById(com.example.test.R.id.fridgeDate)
        val dateDropBtn: ImageView = findViewById(com.example.test.R.id.DateDropBtn)
        val fridgeIngredientInput: EditText = findViewById(com.example.test.R.id.fridgeIngredientInput)
        val fridgeIngredientNumInput: EditText = findViewById(com.example.test.R.id.fridgeIngredientNumInput)
        val fridgePriceInput: EditText = findViewById(com.example.test.R.id.fridgePriceInput)
        val fridgeAddButton: LinearLayout = findViewById(com.example.test.R.id.fridgeAddButton)
        val backBtn: ImageView = findViewById(com.example.test.R.id.backBtn)

        // 드롭다운 로직
        fridgeStorageDropBtn.setOnClickListener {
            showPopupMenu(fridgeStorageDropBtn, fridgeStorageText, com.example.test.R.menu.fridge_storage_menu)
        }
        dateDropBtn.setOnClickListener {
            showPopupMenu(dateDropBtn, fridgeDateDropText, com.example.test.R.menu.fridge_date_menu)
        }
        fridgeUnitDropBtn2.setOnClickListener {
            val popup = PopupMenu(this, fridgeUnitDropBtn2)
            popup.menu.add("무게")
            popup.menu.add("부피")
            popup.menu.add("개수")
            popup.setOnMenuItemClickListener { item: MenuItem ->
                selectedUnitCategory = item.title.toString()
                fridgeIngredientUnit2.text = selectedUnitCategory
                fridgeIngredientUnit.text = "선택" // 세부 단위 초기화
                true
            }
            popup.show()
        }
        fridgeUnitDropBtn.setOnClickListener {
            val popup = PopupMenu(this, fridgeUnitDropBtn)
            when (selectedUnitCategory) {
                "무게" -> {
                    popup.menu.add("kg")
                    popup.menu.add("g")
                    popup.menu.add("근")
                    popup.menu.add("푼")
                }
                "부피" -> {
                    popup.menu.add("mL")
                    popup.menu.add("L")
                    popup.menu.add("스푼")
                    popup.menu.add("컵")
                }
                "개수" -> {
                    popup.menu.add("알")
                    popup.menu.add("봉지")
                    popup.menu.add("포기")
                    popup.menu.add("개")
                    popup.menu.add("통")
                    popup.menu.add("장")
                    popup.menu.add("마리")
                }
                else -> {
                    popup.menu.add("선택")
                }
            }
            popup.setOnMenuItemClickListener { item: MenuItem ->
                fridgeIngredientUnit.text = item.title.toString()
                true
            }
            popup.show()
        }

        // 전달받은 수정 데이터 처리 (수정 시)
        val intentIngredientName = intent.getStringExtra("ingredientName") ?: ""
        val intentQuantity = intent.getStringExtra("quantity") ?: ""
        val intentUnit = intent.getStringExtra("unit") ?: ""
        val intentStorageArea = intent.getStringExtra("storageArea") ?: ""
        val intentFridgeDate = intent.getStringExtra("fridgeDate") ?: ""
        val intentDateOption = intent.getStringExtra("dateOption") ?: ""

        if (intentIngredientName.isNotEmpty()) {
            fridgeIngredientInput.setText(intentIngredientName)
        }
        if (intentQuantity.isNotEmpty()) {
            fridgeIngredientNumInput.setText(intentQuantity)
        }
        if (intentUnit.isNotEmpty()) {
            fridgeIngredientUnit.text = intentUnit
        }
        if (intentStorageArea.isNotEmpty()) {
            fridgeStorageText.text = intentStorageArea
        }
        if (intentFridgeDate.isNotEmpty()) {
            fridgeDateInput.setText(intentFridgeDate)
        }
        if (intentDateOption.isNotEmpty()) {
            fridgeDateDropText.text = intentDateOption
            selectedUnitCategory = intentDateOption
            fridgeIngredientUnit2.text = intentDateOption
        }

        // "추가하기" 버튼 클릭: 로그인한 사용자가 재료 저장
        fridgeAddButton.setOnClickListener {
            // 사용자 입력 데이터 수집
            val ingredientName = fridgeIngredientInput.text.toString().trim()
            val storageArea = fridgeStorageText.text.toString().trim()
            val fridgeDate = fridgeDateInput.text.toString().trim()  // "YYYY-MM-DD" 형식
            val dateOption = fridgeDateDropText.text.toString().trim()
            val quantity = fridgeIngredientNumInput.text.toString().toDoubleOrNull() ?: 0.0
            val price = fridgePriceInput.text.toString().toDoubleOrNull() ?: 0.0

            // 단위 범주 변환: 클라이언트에서는 "개수" 등으로 선택하지만, 서버는 "COUNT"를 원함
            val unitCategoryMapping = mapOf(
                "무게" to "WEIGHT",
                "부피" to "VOLUME",
                "개수" to "COUNT"
            )
            val rawUnitCategory = fridgeIngredientUnit2.text.toString().trim()
            val unitCategory = unitCategoryMapping[rawUnitCategory] ?: rawUnitCategory

            val unitDetail = fridgeIngredientUnit.text.toString().trim()  // 예: "kg"

            // 로그인한 사용자의 ID 및 토큰 가져오기 (PrefApp.prefs 사용)
            val userId = getLoggedInUserId()
            val tokenFromPrefs = getTokenFromPreferences()
            val token = "Bearer $tokenFromPrefs"

            // 디버깅: SharedPreferences에서 가져온 값 출력
            println("getLoggedInUserId(): $userId")
            println("getTokenFromPreferences(): $tokenFromPrefs")
            println("Final token used: $token")

            // FridgeRequest DTO 생성
            val fridgeRequest = FridgeRequest(
                ingredientName = ingredientName,
                storageArea = storageArea,
                fridgeDate = fridgeDate,
                dateOption = dateOption,
                quantity = quantity,
                price = price,
                unitCategory = unitCategory,
                unitDetail = unitDetail,
                userId = userId
            )

            // Retrofit API 호출 (토큰과 함께)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.createFridge(token, fridgeRequest)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            startActivity(Intent(this@FridgeIngredientActivity, FridgeActivity::class.java))
                        } else {
                            Toast.makeText(
                                this@FridgeIngredientActivity,
                                "저장 실패: HTTP ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@FridgeIngredientActivity,
                            "네트워크 오류: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        backBtn.setOnClickListener {
            startActivity(Intent(this, FridgeActivity::class.java))
        }
    }

    private fun showPopupMenu(anchor: ImageView, textView: TextView, menuRes: Int) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(menuRes, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            textView.text = item.title
            true
        }
        popupMenu.show()
    }

    private fun getLoggedInUserId(): Long {
        // App.prefs를 통해 전역적으로 저장된 값 사용 (Prefs 파일 이름 "mPref")
        return App.prefs.userId
    }

    private fun getTokenFromPreferences(): String {
        return App.prefs.token ?: ""
    }
}

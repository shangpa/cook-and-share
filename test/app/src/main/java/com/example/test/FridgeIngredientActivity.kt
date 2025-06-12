package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.test.model.Fridge.FridgeRequest
import com.example.test.model.LoginInfoResponse
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FridgeIngredientActivity : AppCompatActivity() {

    private var selectedUnitCategory: String = "무게"
    private lateinit var apiService: com.example.test.network.ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge_ingredient)

        // tapVillageKitchenIcon 클릭했을 때 MaterialActivity 이동
        val tapVillageKitchenIcon: ImageView = findViewById(R.id.tapVillageKitchenIcon)
        tapVillageKitchenIcon.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        // tapVillageKitchenText 클릭했을 때 MaterialActivity 이동
        val tapVillageKitchenText: TextView = findViewById(R.id.tapVillageKitchenText)
        tapVillageKitchenText.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        // tapRecipeIcon 클릭했을 때 RecipeSeeMainActivity 이동
        val tapRecipeIcon: ImageView = findViewById(R.id.tapRecipeIcon)
        tapRecipeIcon.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // tapRecipeText 클릭했을 때 RecipeSeeMainActivity 이동
        val tapRecipeText: TextView = findViewById(R.id.tapRecipeText)
        tapRecipeText.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // tapHomeIcon 클릭했을 때 MainActivity 이동
        val tapHomeIcon: ImageView = findViewById(R.id.tapHomeIcon)
        tapHomeIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // tapCommunityIcon 클릭했을 때 CommunityMainActivity 이동
        val tapCommunityIcon: ImageView = findViewById(R.id.tapCommunityIcon)
        tapCommunityIcon.setOnClickListener {
            val intent = Intent(this, CommunityMainActivity::class.java)
            startActivity(intent)
        }

        // tapCommunityText 클릭했을 때 CommunityMainActivity 이동
        val tapCommunityText: TextView = findViewById(R.id.tapCommunityText)
        tapCommunityText.setOnClickListener {
            val intent = Intent(this, CommunityMainActivity::class.java)
            startActivity(intent)
        }

        // tapFridgeIcon 클릭했을 때 FridgeActivity 이동
        val tapFridgeIcon: ImageView = findViewById(R.id.tapFridgeIcon)
        tapFridgeIcon.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // tapFridgeText 클릭했을 때 FridgeActivity 이동
        val tapFridgeText: TextView = findViewById(R.id.tapFridgeText)
        tapFridgeText.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

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
        val fridgeAddButton : AppCompatButton = findViewById(R.id.fridgeAddButton)
        val backBtn: ImageView = findViewById(R.id.backBtn)
        val nameInput = findViewById<EditText>(R.id.fridgeIngredientInput)
        val countInput = findViewById<EditText>(R.id.fridgeIngredientNumInput)
        val dateInput = findViewById<EditText>(R.id.fridgeDateInput)
        val unitText = findViewById<TextView>(R.id.fridgeIngredientUnit)
        val unitText2 = findViewById<TextView>(R.id.fridgeIngredientUnit2)
        val addButton = findViewById<AppCompatButton>(R.id.fridgeAddButton)

        val intentIngredientName = intent.getStringExtra("ingredientName") ?: ""
        if (intentIngredientName.isNotEmpty()) {
            fridgeIngredientInput.setText(intentIngredientName)
        }

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

        Log.d("FridgeIngredientActivity", "수정용 fridgeId: $intentFridgeId")

        // 입력값 확인 후 버튼 상태 갱신 함수
        fun updateButtonState() {
            val nameFilled = nameInput.text.toString().trim().isNotEmpty()
            val countFilled = countInput.text.toString().trim().isNotEmpty()
            val dateFilled = dateInput.text.toString().trim().isNotEmpty()
            val unitFilled = unitText.text.toString().trim() != "선택"

            if (nameFilled && countFilled && dateFilled && unitFilled) {
                addButton.setBackgroundResource(R.drawable.btn_big_green)
                addButton.setTextColor(Color.parseColor("#FFFFFF"))
            } else {
                addButton.setBackgroundResource(R.drawable.btn_number_of_people)
                addButton.setTextColor(Color.parseColor("#A1A9AD"))
            }
        }

        // TextWatcher 공통 등록 함수
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updateButtonState()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        // EditText에 TextWatcher 등록
        nameInput.addTextChangedListener(watcher)
        countInput.addTextChangedListener(watcher)
        dateInput.addTextChangedListener(watcher)

        // 단위 선택 시 unitText 값이 변경되면 updateButtonState 호출 필요
        unitText.setOnClickListener {
            // 예: 단위 선택 다이얼로그 후 선택 시
            // unitText.text = "개"
            updateButtonState()
        }

        // 뒤로가기 버튼
        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }

        val fridgeUserText = findViewById<TextView>(R.id.fridgeUserText)
        val token = "Bearer ${getTokenFromPreferences()}"
        val call = RetrofitInstance.apiService.getUserInfo(token)

        call.enqueue(object : Callback<LoginInfoResponse> {
            override fun onResponse(
                call: Call<LoginInfoResponse>,
                response: Response<LoginInfoResponse>
            ) {
                if (response.isSuccessful) {
                    val userName = response.body()?.name
                    fridgeUserText.text = "$userName 님이 가진 재료를 입력해보세요!"
                } else {
                    fridgeUserText.text = "로그인 후 가지고 있는 재료를 입력해보세요!"
                }
            }

            override fun onFailure(call: Call<LoginInfoResponse>, t: Throwable) {
                fridgeUserText.text = "오류 발생"
            }
        })

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

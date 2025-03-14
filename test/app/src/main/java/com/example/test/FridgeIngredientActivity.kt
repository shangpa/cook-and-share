package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FridgeIngredientActivity : AppCompatActivity() {

    // 현재 선택된 단위 범주, 기본값 "무게"
    private var selectedUnitCategory: String = "무게"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge_ingredient)

        // 기존 뷰 참조
        val fridgeStorageText: TextView = findViewById(R.id.fridgeStorageText)
        val fridgeStorageDropBtn: ImageView = findViewById(R.id.fridgeStorageDropBtn)
        // 세부 단위를 표시할 TextView
        val fridgeIngredientUnit: TextView = findViewById(R.id.fridgeIngredientUnit)
        // 단위 범주를 표시할 TextView (이 값은 변경하지 않음)
        val fridgeIngredientUnit2: TextView = findViewById(R.id.fridgeIngredientUnit2)
        // 드롭다운 버튼들
        val fridgeUnitDropBtn: ImageView = findViewById(R.id.fridgeUnitDropBtn)
        val fridgeUnitDropBtn2: ImageView = findViewById(R.id.fridgeUnitDropBtn2)
        // 날짜 관련 뷰
        val fridgeDateInput: EditText = findViewById(R.id.fridgeDateInput)
        val fridgeDateDropText: TextView = findViewById(R.id.fridgeDate)
        val dateDropBtn: ImageView = findViewById(R.id.DateDropBtn)
        // 기타
        val fridgeIngredientInput: EditText = findViewById(R.id.fridgeIngredientInput)
        val fridgeIngredientNumInput: EditText = findViewById(R.id.fridgeIngredientNumInput)
        val fridgeAddButton: LinearLayout = findViewById(R.id.fridgeAddButton)
        val backBtn: ImageView = findViewById(R.id.backBtn)

        // 보관장소 드롭다운
        fridgeStorageDropBtn.setOnClickListener {
            showPopupMenu(fridgeStorageDropBtn, fridgeStorageText, R.menu.fridge_storage_menu)
        }
        // 날짜 드롭다운
        dateDropBtn.setOnClickListener {
            showPopupMenu(dateDropBtn, fridgeDateDropText, R.menu.fridge_date_menu)
        }

        // 단위 범주 드롭다운 (fridgeUnitDropBtn2)
        fridgeUnitDropBtn2.setOnClickListener {
            val popup = PopupMenu(this, fridgeUnitDropBtn2)
            popup.menu.add("무게")
            popup.menu.add("부피")
            popup.menu.add("개수")
            popup.setOnMenuItemClickListener { item: MenuItem ->
                selectedUnitCategory = item.title.toString()
                // fridgeIngredientUnit2는 단위 범주를 표시(예, "무게")
                fridgeIngredientUnit2.text = selectedUnitCategory
                // reset 세부 단위는 "선택"으로 초기화
                fridgeIngredientUnit.text = "선택"
                true
            }
            popup.show()
        }

        // 세부 단위 드롭다운 (fridgeUnitDropBtn)
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
                // 선택한 세부 단위가 fridgeIngredientUnit에 표시됨.
                fridgeIngredientUnit.text = item.title.toString()
                true
            }
            popup.show()
        }

        // 편집 시 전달받은 데이터 (수정할 경우)
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

        // "추가하기" 버튼: 전달할 데이터 구성
        fridgeAddButton.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            val ingredientName = fridgeIngredientInput.text.toString()
            val storageArea = fridgeStorageText.text.toString()
            val fridgeDate = fridgeDateInput.text.toString()
            val dateOption = fridgeDateDropText.text.toString()
            val quantity = fridgeIngredientNumInput.text.toString()
            val unit = fridgeIngredientUnit.text.toString()

            intent.putExtra("ingredientName", ingredientName)
            intent.putExtra("storageArea", storageArea)
            intent.putExtra("fridgeDate", fridgeDate)
            intent.putExtra("dateOption", dateOption)
            intent.putExtra("quantity", quantity)
            intent.putExtra("unit", unit)
            startActivity(intent)
        }

        backBtn.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
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
}

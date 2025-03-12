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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge_ingredient)

        val fridgeStorageText: TextView = findViewById(R.id.fridgeStorageText)
        val fridgeStorageDropBtn: ImageView = findViewById(R.id.fridgeStorageDropBtn)
        val fridgeIngredientUnit: TextView = findViewById(R.id.fridgeIngredientUnit)
        val fridgeUnitDropBtn: ImageView = findViewById(R.id.fridgeUnitDropBtn)
        // EditText로 날짜 입력 받음
        val fridgeDateInput: EditText = findViewById(R.id.fridgeDateInput)
        // 드롭다운 결과를 보여줄 TextView (예: "유통기한" 또는 "구매일자")
        val fridgeDateDropText: TextView = findViewById(R.id.fridgeDate)
        val dateDropBtn: ImageView = findViewById(R.id.DateDropBtn)
        val fridgeAddButton: LinearLayout = findViewById(R.id.fridgeAddButton)
        val backBtn: ImageView = findViewById(R.id.backBtn)

        fridgeStorageDropBtn.setOnClickListener {
            showPopupMenu(fridgeStorageDropBtn, fridgeStorageText, R.menu.fridge_storage_menu)
        }

        fridgeUnitDropBtn.setOnClickListener {
            showPopupMenu(fridgeUnitDropBtn, fridgeIngredientUnit, R.menu.fridge_unit_menu)
        }

        dateDropBtn.setOnClickListener {
            showPopupMenu(dateDropBtn, fridgeDateDropText, R.menu.fridge_date_menu)
        }

        // 미리 데이터를 전달받은 경우, 입력 필드에 세팅 (편집 시)
        val intentIngredientName = intent.getStringExtra("ingredientName") ?: ""
        val intentQuantity = intent.getStringExtra("quantity") ?: ""
        val intentUnit = intent.getStringExtra("unit") ?: ""
        val intentStorageArea = intent.getStringExtra("storageArea") ?: ""
        // 날짜 값: "fridgeDate" 키 사용
        val intentFridgeDate = intent.getStringExtra("fridgeDate") ?: ""
        // 드롭다운 결과: "dateOption" 키 사용
        val intentDateOption = intent.getStringExtra("dateOption") ?: ""

        if (intentIngredientName.isNotEmpty()) {
            val fridgeIngredientInput: EditText = findViewById(R.id.fridgeIngredientInput)
            fridgeIngredientInput.setText(intentIngredientName)
        }
        if (intentQuantity.isNotEmpty()) {
            val fridgeIngredientNumInput: EditText = findViewById(R.id.fridgeIngredientNumInput)
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
        }

        fridgeAddButton.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)

            // 값 가져오기
            val ingredientName = findViewById<EditText>(R.id.fridgeIngredientInput).text.toString()
            val storageArea = fridgeStorageText.text.toString()
            val fridgeDate = fridgeDateInput.text.toString()
            val dateOption = fridgeDateDropText.text.toString()
            val quantity = findViewById<EditText>(R.id.fridgeIngredientNumInput).text.toString()
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

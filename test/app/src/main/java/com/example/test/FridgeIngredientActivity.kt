package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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
        val fridgeDate: TextView = findViewById(R.id.fridgeDate)
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
            showPopupMenu(dateDropBtn, fridgeDate, R.menu.fridge_date_menu)
        }

        fridgeAddButton.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
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

/*냉장고 재료 관리 - 재료 관리하기*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class FridgeIngredientActivity  : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge_ingredient)

        // fridgeAddButton 클릭했을 때 MaterialSalesActivity 이동
        val fridgeAddButton: LinearLayout = findViewById(R.id.fridgeAddButton)
        fridgeAddButton.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // backBtn 클릭했을 때 MaterialSalesActivity 이동
        val backBtn: ImageView = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }
    }
}
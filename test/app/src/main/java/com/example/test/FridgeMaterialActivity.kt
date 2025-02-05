/*냉장고 재료 관리 - 재료 관리하기*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class FridgeMaterialActivity  : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge_material)

        // fridgeAddButton 클릭했을 때 MaterialSalesActivity 이동
        val fridgeAddButton: LinearLayout = findViewById(R.id.fridgeAddButton)
        fridgeAddButton.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // fridgeMaterialBackButton 클릭했을 때 MaterialSalesActivity 이동
        val fridgeMaterialBackButton: ImageView = findViewById(R.id.fridgeMaterialBackButton)
        fridgeMaterialBackButton.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }
    }
}
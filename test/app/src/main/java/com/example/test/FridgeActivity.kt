/*냉장고 재료 관리 메인*/
package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class FridgeActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)

        // fridgeAddMaterial 클릭했을 때 NoticeActivity 이동
        val fridgeAddMaterial: LinearLayout = findViewById(R.id.fridgeAddMaterial)
        fridgeAddMaterial.setOnClickListener {
            val intent = Intent(this, FridgeMaterialActivity::class.java)
            startActivity(intent)
        }

        // recipeRecomend 클릭했을 때 NoticeActivity 이동
        val recipeRecomend: LinearLayout = findViewById(R.id.recipeRecomend)
        recipeRecomend.setOnClickListener {
            val intent = Intent(this, FridgeRecipeActivity::class.java)
            startActivity(intent)
        }
    }
}
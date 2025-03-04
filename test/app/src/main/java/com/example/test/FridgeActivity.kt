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

        // fridgeAddBtn 클릭했을 때 FridgeMaterialActivity 이동
        val fridgeAddBtn: LinearLayout = findViewById(R.id.fridgeAddBtn)
        fridgeAddBtn.setOnClickListener {
            val intent = Intent(this, FridgeIngredientActivity::class.java)
            startActivity(intent)
        }

        // recipeRecommendBtn 클릭했을 때 FridgeRecipeActivity 이동
        val recipeRecommendBtn: LinearLayout = findViewById(R.id.recipeRecommendBtn)
        recipeRecommendBtn.setOnClickListener {
            val intent = Intent(this, FridgeRecipeActivity::class.java)
            startActivity(intent)
        }
    }
}
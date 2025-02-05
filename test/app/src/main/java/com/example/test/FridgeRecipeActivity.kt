/*냉장고 재료 관리 - 재료를 통한 레시피 추천*/
package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class FridgeRecipeActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge_recipe)

        // fridgeRecipe1 클릭했을 때 RecipeMainOne 이동
        val fridgeRecipe1: LinearLayout = findViewById(R.id.fridgeRecipe1)
        fridgeRecipe1.setOnClickListener {
            val intent = Intent(this, RecipeMainOne::class.java)
            startActivity(intent)
        }
    }
}
/*전체 레시피*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeTotal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipe_tap)

        // recipeWrite 클릭했을 때 NoticeActivity 이동
        val recipeWrite: TextView = findViewById(R.id.recipeWrite)
        recipeWrite.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothTitle::class.java)
            startActivity(intent)
        }
    }
}
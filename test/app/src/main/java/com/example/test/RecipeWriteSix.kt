/*레시피 작성 STEP6*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RecipeWriteSix : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipe_write6)

        // buyFixButton 클릭했을 때 NoticeActivity 이동
        val buyFixButton: Button = findViewById(R.id.buyFixButton)
        buyFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteSeven::class.java)
            startActivity(intent)
        }
    }
}
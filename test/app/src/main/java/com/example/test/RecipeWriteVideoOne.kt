/*레시피 작성 동영상 STEP1*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeWriteVideoOne : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipe_write9)

        // nextFixButton 클릭했을 때 RecipeWriteVideoTwo 이동
        val nextFixButton: TextView = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoTwo::class.java)
            startActivity(intent)
        }

        // image 클릭했을 때 RecipeWriteTwo 이동
        val image: TextView = findViewById(R.id.image)
        image.setOnClickListener {
            val intent = Intent(this, RecipeWriteTwo::class.java)
            startActivity(intent)
        }
    }
}

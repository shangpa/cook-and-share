/*레시피 작성 STEP2*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeWriteTwo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipe_write2)

        // nextFixButton 클릭했을 때 NoticeActivity 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteThree::class.java)
            startActivity(intent)
        }

        // video 클릭했을 때 RecipeWriteVideoOne 이동
        val video: TextView = findViewById(R.id.video)
        video.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoOne::class.java)
            startActivity(intent)
        }
    }
}

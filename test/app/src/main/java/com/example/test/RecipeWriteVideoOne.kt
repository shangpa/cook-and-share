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

        // video 클릭했을 때 NoticeActivity 이동
        val video: TextView = findViewById(R.id.video)
        video.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoTwo::class.java)
            startActivity(intent)
        }
    }
}
/*레시피 작성 동영상 STEP2*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeWriteVideoTwo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipe_write10)

        // video 클릭했을 때 NoticeActivity 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoComplete::class.java)
            startActivity(intent)
        }
    }
}
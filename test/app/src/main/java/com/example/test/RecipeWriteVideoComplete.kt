/*레시피 작성 동영상 완료*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RecipeWriteVideoComplete : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipe_write21)

        // video 클릭했을 때 NoticeActivity 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, VideoSee::class.java)
            startActivity(intent)
        }
    }
}
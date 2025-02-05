/*비디오 상세*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class VideoSee : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_see)

        // backArrow 클릭했을 때 NoticeActivity 이동
        val backArrow: ImageButton = findViewById(R.id.backArrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, RecipeWriteSeven::class.java)
            startActivity(intent)
        }
    }
}
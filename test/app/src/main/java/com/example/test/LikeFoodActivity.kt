/*선호 요리 선택*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class LikeFoodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like_food)

        // LikeFoodBackButton 클릭했을 때 MypageActivity 이동
        val LikeFoodBackButton: ImageView = findViewById(R.id.LikeFoodBackButton)
        LikeFoodBackButton.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }
    }
}

/*아이디 찾기 - 메인(step 1)*/
package com.example.test

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout

class FindIDMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_id_main)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
}
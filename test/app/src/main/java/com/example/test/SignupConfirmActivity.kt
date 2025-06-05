/*회원가입 - 본인인증(step 2)*/
package com.example.test

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout

class SignupConfirmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_confirm)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
}
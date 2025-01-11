package com.example.test

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginId = findViewById<EditText>(R.id.etLoginId)
        val loginPassword = findViewById<EditText>(R.id.etLoginPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)

        // TextWatcher 객체를 생성
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginButton.isEnabled = loginId.text.isNotEmpty() && loginPassword.text.isNotEmpty()
                loginButton.backgroundTintList = ColorStateList.valueOf(
                    if (loginButton.isEnabled) Color.parseColor("#35A825") else Color.LTGRAY
                )
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        // TextWatcher 객체 추가
        loginId.addTextChangedListener(textWatcher)
        loginPassword.addTextChangedListener(textWatcher)
    }
}

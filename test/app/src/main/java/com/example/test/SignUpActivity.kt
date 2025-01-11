package com.example.test

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // 뷰 바인딩
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val idEditText = findViewById<EditText>(R.id.idEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val continueButton = findViewById<Button>(R.id.continueButton)
        val passwordMismatchMessage = findViewById<TextView>(R.id.passwordMismatchMessage)

        // 모든 입력 필드가 채워졌는지 확인하는 함수
        val checkFields = {
            val isNameFilled = nameEditText.text.toString().isNotEmpty()
            val isIdFilled = idEditText.text.toString().isNotEmpty()
            val isPasswordFilled = passwordEditText.text.toString().isNotEmpty()
            val isConfirmPasswordFilled = confirmPasswordEditText.text.toString().isNotEmpty()
            val isPasswordsMatch =
                passwordEditText.text.toString() == confirmPasswordEditText.text.toString()

            if (isNameFilled && isIdFilled && isPasswordFilled && isConfirmPasswordFilled) {
                if (isPasswordsMatch) {
                    continueButton.isEnabled = true
                    continueButton.setBackgroundColor(Color.parseColor("#4CAF50")) // 초록색
                    continueButton.setTextColor(Color.WHITE) // 버튼 텍스트 색상
                    passwordMismatchMessage.visibility = TextView.GONE
                } else {
                    continueButton.isEnabled = false
                    continueButton.setBackgroundColor(Color.parseColor("#BDBDBD")) // 회색
                    continueButton.setTextColor(Color.GRAY) // 버튼 텍스트 색상
                    passwordMismatchMessage.visibility = TextView.VISIBLE
                }
            } else {
                continueButton.isEnabled = false
                continueButton.setBackgroundColor(Color.parseColor("#BDBDBD")) // 회색
                continueButton.setTextColor(Color.GRAY) // 버튼 텍스트 색상
                passwordMismatchMessage.visibility = TextView.GONE
            }
        }

        // TextWatcher를 설정
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkFields()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        // 입력 필드에 TextWatcher 추가
        nameEditText.addTextChangedListener(textWatcher)
        idEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(textWatcher)
        confirmPasswordEditText.addTextChangedListener(textWatcher)
    }
}

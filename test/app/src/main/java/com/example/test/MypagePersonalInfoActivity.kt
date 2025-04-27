package com.example.test

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R

class MypagePersonalInfoActivity : AppCompatActivity() {
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var passwordUnderline: View
    private lateinit var confirmUnderline: View
    private lateinit var passwordErrorText: TextView
    private lateinit var passwordMismatchText: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_personal_info)

        passwordInput = findViewById(R.id.passwordInput) // 비밀번호 EditText id 직접 설정해줘야 함
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        passwordUnderline = findViewById(R.id.passwordUnderline)
        confirmUnderline = findViewById(R.id.confirmUnderline)
        passwordErrorText = findViewById(R.id.passwordErrorText)
        passwordMismatchText = findViewById(R.id.passwordMismatchText)

        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                val pattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,15}\$")

                if (!pattern.matches(password)) {
                    passwordErrorText.visibility = View.VISIBLE
                    passwordUnderline.setBackgroundColor(Color.RED)
                } else {
                    passwordErrorText.visibility = View.GONE
                    passwordUnderline.setBackgroundColor(Color.BLUE)
                }

                checkPasswordMatch()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        confirmPasswordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkPasswordMatch()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun checkPasswordMatch() {
        val pw = passwordInput.text.toString()
        val confirm = confirmPasswordInput.text.toString()

        if (confirm.isNotEmpty() && pw != confirm) {
            passwordMismatchText.visibility = View.VISIBLE
            confirmUnderline.setBackgroundColor(Color.RED)
        } else if (confirm.isNotEmpty() && pw == confirm) {
            passwordMismatchText.visibility = View.GONE
            confirmUnderline.setBackgroundColor(Color.BLUE)
        }
    }
}

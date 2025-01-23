package com.example.test

import Prefs
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.model.LoginRequest
import com.example.test.model.LoginResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<Button>(R.id.btnLogin)
        val loginId = findViewById<EditText>(R.id.etLoginId)
        val loginPassword = findViewById<EditText>(R.id.etLoginPassword)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        // 로그인 기능
        loginButton.setOnClickListener {
            val username = loginId.text.toString()
            val password = loginPassword.text.toString()
            val loginRequest = LoginRequest(username, password)
            Log.e("LoginActivity", "Request: ${loginRequest}")  // 요청 본문 확인용 로그
            Log.e("LoginActivity", "회원가입 요청 - username: $username, password: $password")

            RetrofitInstance.apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null && loginResponse.message == "Login successful") {
                            // 로그인 성공, 토큰 저장
                            saveToken(loginResponse.token)
                            Log.e("LoginActivity", "저장된 토큰================= : ${App.prefs.token}")

                            Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@LoginActivity, LoginInfoActivity::class.java)
                            // 로그인 성공시 loginInfoActivity로 이동
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "로그인 실패: ${loginResponse?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "로그인 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    // 로그캣에 오류 메시지 출력
                    Log.e("LoginActivity", "네트워크 오류: ${t.message}", t)
                }
            })
        }

        tvSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveToken(token: String) {
        val prefs = Prefs(this)
        prefs.token = token
        Log.e("LoginActivity", "토큰 저장 성공: $token")
    }
}
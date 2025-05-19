package com.example.test

import Prefs
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.model.LoginRequest
import com.example.test.model.LoginResponse
import com.example.test.model.notification.FcmTokenRequestDTO
import com.example.test.network.RetrofitInstance
import com.google.firebase.messaging.FirebaseMessaging
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

        loginButton.setOnClickListener {
            val username = loginId.text.toString().trim()
            val password = loginPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(username, password)
            Log.e("LoginActivity", "Request: $loginRequest")
            Log.e("LoginActivity", "로그인 요청 - username: $username, password: $password")

            RetrofitInstance.apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null && loginResponse.message == "Login successful") {
                            // 로그인 성공: 서버에서 받은 사용자 ID와 JWT 토큰 저장
                            saveAuthData(loginResponse.userId, loginResponse.token)
                            Log.e("LoginActivity", "저장된 토큰: ${App.prefs.token}")
                            Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()

                            //FCM 토큰 받아서 서버에 전송
                            FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                                Log.d("FCM", "FCM 토큰: $fcmToken")

                                val request = FcmTokenRequestDTO(token = fcmToken, platform = "ANDROID")
                                val authToken = App.prefs.token ?: return@addOnSuccessListener

                                RetrofitInstance.notificationApi.sendFcmToken(
                                    "Bearer $authToken",
                                    request
                                ).enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                        Log.d("FCM", "FCM 토큰 서버 전송 성공")
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                        Log.e("FCM", "FCM 토큰 서버 전송 실패", t)
                                    }
                                })
                            }

                            // 로그인 성공 후 LoginInfoActivity 또는 메인 화면으로 이동
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
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
                    Log.e("LoginActivity", "네트워크 오류: ${t.message}", t)
                }
            })
        }

        tvSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    // 로그인 성공 시 SharedPreferences에 사용자 ID와 JWT 토큰 저장
    private fun saveAuthData(userId: Long, token: String) {
        val prefs = Prefs(this)
        prefs.userId = userId
        prefs.token = token
        Log.e("LoginActivity", "토큰 저장 성공: $token, userId: $userId")
    }
}

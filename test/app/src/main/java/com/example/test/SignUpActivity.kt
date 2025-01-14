package com.example.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.model.ApiResponse
import com.example.test.model.SignUpRequest
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val continueButton = findViewById<Button>(R.id.continueButton)
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val idEditText = findViewById<EditText>(R.id.idEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val backButton = findViewById<ImageView>(R.id.backButton)

        // 뒤로가기 버튼 클릭 리스너
        backButton.setOnClickListener {
            finish() // SignUpActivity 종료하고 이전 액티비티로 돌아가기
        }

        continueButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val username = idEditText.text.toString()
            val password = passwordEditText.text.toString()

            val signUpRequest = SignUpRequest(name, username, password)

            // Retrofit 호출
            RetrofitInstance.apiService.signUp(signUpRequest).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.success) {
                            // 회원가입 성공 후 로그인 화면으로 이동
                            Toast.makeText(this@SignUpActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish() // 현재 액티비티 종료
                        } else {
                            // 실패한 경우 메시지 출력
                            Log.e("SignUpActivity", "회원가입 실패: ${apiResponse?.message}")
                            Toast.makeText(this@SignUpActivity, "회원가입 실패: ${apiResponse?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 서버 응답 실패 처리
                        Log.e("SignUpActivity", "회원가입 실패: ${response.message()}")
                        Toast.makeText(this@SignUpActivity, "회원가입 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    // 네트워크 오류 처리
                    Toast.makeText(this@SignUpActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("SignUpActivity", "네트워크 오류: ${t.message}", t)
                }
            })
        }
    }
}

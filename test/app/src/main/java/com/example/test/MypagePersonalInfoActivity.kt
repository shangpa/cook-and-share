package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.test.model.LoginInfoResponse
import com.example.test.model.UpdateUserRequest
import com.example.test.network.RetrofitInstance

class MypagePersonalInfoActivity : AppCompatActivity() {
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var passwordUnderline: View
    private lateinit var confirmUnderline: View
    private lateinit var passwordErrorText: TextView
    private lateinit var passwordMismatchText: TextView
    private lateinit var passwordNowInput: EditText
    private lateinit var passwordNowUnderline: View
    private lateinit var passwordNowErrorText: TextView

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
        passwordNowInput = findViewById(R.id.passwordNowInput)
        passwordNowUnderline = findViewById(R.id.passwordNowUnderline)
        passwordNowErrorText = findViewById(R.id.passwordNowErrorText)

        passwordNowInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val inputPassword = s.toString()
                checkCurrentPassword(inputPassword)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

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

        val usernameEditText = findViewById<EditText>(R.id.personalInfoID)
        usernameEditText.isEnabled = false

        RetrofitInstance.apiService.getUserInfo("Bearer ${App.prefs.token}")
            .enqueue(object : Callback<LoginInfoResponse> {
                override fun onResponse(call: Call<LoginInfoResponse>, response: Response<LoginInfoResponse>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        findViewById<TextView>(R.id.personalInfoName).text = data?.name
                        usernameEditText.setText(data?.userName)
                    }
                }

                override fun onFailure(call: Call<LoginInfoResponse>, t: Throwable) {
                    Log.e("개인정보 요청 실패", t.message ?: "알 수 없는 오류")
                }
            })

        val btnInfoFix = findViewById<LinearLayout>(R.id.btnInfoFix)

        btnInfoFix.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordInput.text.toString()

            //현재 비밀번호와 일치하는지 여부 판단
            if (passwordNowErrorText.visibility == View.VISIBLE) {
                Toast.makeText(this, "현재 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 비밀번호 유효성 검사 실패 시 중단
            if (passwordErrorText.visibility == View.VISIBLE || passwordMismatchText.visibility == View.VISIBLE) {
                Toast.makeText(this, "비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val request = UpdateUserRequest(password = password)

            RetrofitInstance.apiService.updateUserInfo("Bearer ${App.prefs.token}", request)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MypagePersonalInfoActivity, "비밀번호 수정 완료! 다시 로그인해주세요", Toast.LENGTH_SHORT).show()

                            // 토큰 제거 (로그아웃)
                            App.prefs.token = ""

                            // 로그인 화면으로 이동
                            val intent = Intent(this@MypagePersonalInfoActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)

                            // 현재 액티비티 종료
                            finishAffinity()
                        } else {
                            Toast.makeText(this@MypagePersonalInfoActivity, "수정 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@MypagePersonalInfoActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                    }
                })

        }

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

    }

    private fun checkCurrentPassword(input: String) {
        val body = mapOf("password" to input)
        RetrofitInstance.apiService.checkPassword("Bearer ${App.prefs.token}", body)
            .enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    val match = response.body() ?: false
                    if (!match) {
                        passwordNowErrorText.visibility = View.VISIBLE
                        passwordNowUnderline.setBackgroundColor(Color.RED)
                    } else {
                        passwordNowErrorText.visibility = View.GONE
                        passwordNowUnderline.setBackgroundColor(Color.BLUE)
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Log.e("비밀번호 확인 실패", t.message ?: "알 수 없는 오류")
                }
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

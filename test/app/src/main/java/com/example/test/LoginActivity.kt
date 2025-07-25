package com.example.test

import Prefs
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.test.model.GoogleLoginRequest
import com.example.test.model.LoginRequest
import com.example.test.model.LoginResponse
import com.example.test.model.notification.FcmTokenRequestDTO
import com.example.test.network.RetrofitInstance
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("207116637821-9s9rbj2mn86707fg9khds5o2b78m7h2q.apps.googleusercontent.com")
            .requestEmail()
            .build()
        val loginButton = findViewById<AppCompatButton>(R.id.btnLogin)
        val loginId = findViewById<EditText>(R.id.etLoginId)
        val loginPassword = findViewById<EditText>(R.id.etLoginPassword)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        val loginTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val idFilled = loginId.text.toString().isNotBlank()
                val pwFilled = loginPassword.text.toString().isNotBlank()

                if (idFilled && pwFilled) {
                    loginButton.setBackgroundResource(R.drawable.btn_big_green)
                    loginButton.setTextColor(Color.parseColor("#FFFFFF"))
                } else {
                    loginButton.setBackgroundResource(R.drawable.btn_number_of_people)
                    loginButton.setTextColor(Color.parseColor("#A1A9AD"))
                }
            }
        }

        // 두 EditText에 TextWatcher 등록
        loginId.addTextChangedListener(loginTextWatcher)
        loginPassword.addTextChangedListener(loginTextWatcher)

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
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleLoginButton = findViewById<SignInButton>(R.id.btnGoogleLogin)

        googleLoginButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    // 로그인 성공 시 SharedPreferences에 사용자 ID와 JWT 토큰 저장
    private fun saveAuthData(userId: Long, token: String) {
        val prefs = Prefs(this)
        prefs.userId = userId
        prefs.token = token
        Log.e("LoginActivity", "토큰 저장 성공: $token, userId: $userId")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    sendIdTokenToServer(idToken)
                }
            } catch (e: ApiException) {
                Log.e("GoogleLogin", "Google 로그인 실패", e)
            }
        }

    }
    private fun sendIdTokenToServer(idToken: String) {
        val request = GoogleLoginRequest(idToken)
        RetrofitInstance.apiService.googleLogin(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Log.d("GOOGLE", "response.isSuccessful: ${response.isSuccessful}")
                Log.d("GOOGLE", "response.body(): ${response.body()}")

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    Log.d("GoogleLogin", "로그인 성공: ${loginResponse.token}, ${loginResponse.userId}")

                    App.prefs.token = loginResponse.token
                    App.prefs.userId = loginResponse.userId

                    Toast.makeText(this@LoginActivity, "구글 로그인 성공", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("GoogleLogin", "구글 로그인 실패: code=${response.code()}, error=$errorBody")
                    Toast.makeText(this@LoginActivity, "구글 로그인 실패", Toast.LENGTH_SHORT).show()
                }            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("GoogleLogin", "서버 전송 실패", t)
                Toast.makeText(this@LoginActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}

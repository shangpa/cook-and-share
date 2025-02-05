package com.example.test
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.test.model.LoginInfoResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class LoginInfoActivity : AppCompatActivity() {
    private lateinit var userNameTextView: TextView
    private lateinit var userIdTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logininfo)
        userNameTextView = findViewById(R.id.userNameTextView)
        userIdTextView = findViewById(R.id.userIdTextView)
        logoutButton = findViewById(R.id.logoutButton)
        val token = App.prefs.token
        Log.e("LoginInfoActivity", "가져온 토큰: $token")
        // 토큰이 있을 경우 서버에서 로그인 정보 가져오기
        if (token != null && token.isNotEmpty()) {
            Log.e("LoginInfoActivity", "가져온 토큰: $token")
            fetchUserInfo(token)
        } else {
            Toast.makeText(this, "토큰이 없습니다. 로그인해주세요.", Toast.LENGTH_SHORT).show()
        }
        // 로그아웃 버튼 클릭 리스너 설정
        logoutButton.setOnClickListener {
            // 로그인 정보 삭제
            App.prefs.token = null
            // 로그인 화면으로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun fetchUserInfo(token: String) {
        RetrofitInstance.apiService.getUserInfo("Bearer $token").enqueue(object : Callback<LoginInfoResponse> {
            override fun onResponse(call: Call<LoginInfoResponse>, response: Response<LoginInfoResponse>) {
                Log.e("LoginInfoActivity","로그인 정보 불러오기");
                if (response.isSuccessful) {
                    val userInfo = response.body()
                    if (userInfo != null) {
                        Log.d("LoginInfoActivity", "UserName: ${userInfo.userName}")
                        Log.d("LoginInfoActivity", "Name: ${userInfo.name}")
                        // 로그인 정보 화면에 표시
                        userNameTextView.text = userInfo.userName
                        userIdTextView.text = userInfo.name
                    } else {
                        Toast.makeText(this@LoginInfoActivity, "사용자 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginInfoActivity, "서버 오류: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<LoginInfoResponse>, t: Throwable) {
                Toast.makeText(this@LoginInfoActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // loginButton 클릭했을 때 NoticeActivity 이동
        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
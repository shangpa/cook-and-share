package com.example.test.network
import android.content.Context
import android.content.Intent
import com.example.test.LoginActivity
import com.example.test.App
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = App.prefs.token.toString()

        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(request)

        if (response.code == 401 || response.code == 403) {
            // 토큰 초기화 (로그아웃 효과)
            App.prefs.token = ""

            // 로그인 화면으로 이동
            val intent = Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }

        return response
    }
}
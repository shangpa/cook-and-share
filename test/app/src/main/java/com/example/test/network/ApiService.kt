package com.example.test.network


import com.example.test.model.ApiResponse
import com.example.test.model.LoginRequest
import com.example.test.model.LoginResponse
import com.example.test.model.SignUpRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // 로그인 요청
    @POST("api/auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    // 회원가입 요청
    @POST("api/auth/register")
    fun signUp(@Body signUpRequest: SignUpRequest): Call<ApiResponse>
    abstract fun getUserInfo(s: String): Any
}
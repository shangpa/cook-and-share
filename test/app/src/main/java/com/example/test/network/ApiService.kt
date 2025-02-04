package com.example.test.network


import com.example.test.model.ApiResponse
import com.example.test.model.LoginInfoResponse
import com.example.test.model.LoginRequest
import com.example.test.model.LoginResponse
import com.example.test.model.SignUpRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    // 로그인 요청
    @POST("login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    // 회원가입 요청
    @POST("join")
    fun signUp(@Body signUpRequest: SignUpRequest): Call<ApiResponse>

    @GET("user/info")
    fun getUserInfo(@Header("Authorization") token: String): Call<LoginInfoResponse>
}
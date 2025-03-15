package com.example.test.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {  // 주소 변경해야함
    private const val BASE_URL = "http://172.30.1.71:8080"
    // 실제 API 주소 network_security_config.xml도 변경해야함

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
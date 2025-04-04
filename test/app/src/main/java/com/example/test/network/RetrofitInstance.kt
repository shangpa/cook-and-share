package com.example.test.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    const val BASE_URL = "http://192.168.68.111:8080"

    // 로깅 인터셉터 (필요에 따라 설정, 디버깅 용도)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS // 로그 출력 중단으로 OOM 방지
    }

    // OkHttpClient 생성 (로깅 인터셉터 포함)
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // Retrofit 인스턴스 생성
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)  // OkHttpClient 설정 적용
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ApiService 싱글톤 객체
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

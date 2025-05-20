package com.example.test.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    const val BASE_URL = "http://172.30.1.39:8080"
    //서버는 34.47.85.54

    private lateinit var retrofit: Retrofit
    lateinit var apiService: ApiService
    lateinit var communityApi: CommunityApi
    lateinit var notificationApi: NotificationApi

    // 초기화 메서드에서 context 전달받아 처리
    fun init(context: Context) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val client = OkHttpClient.Builder()
            //.addInterceptor(AuthInterceptor(context)) 주석처리된거 중요한거에요ㅠㅠ
            .addInterceptor(loggingInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        communityApi = retrofit.create(CommunityApi::class.java)
        notificationApi = retrofit.create(NotificationApi::class.java)
    }
}
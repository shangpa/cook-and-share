package com.example.test.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    const val BASE_URL = "http://192.168.0.91:8080"

    private lateinit var retrofit: Retrofit
    lateinit var apiService: ApiService
    lateinit var communityApi: CommunityApi
    lateinit var notificationApi: NotificationApi
    lateinit var chatApi: ChatApi
    lateinit var materialApi: MaterialApi
    // 초기화 메서드에서 context 전달받아 처리
    fun init(context: Context) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
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
        chatApi= retrofit.create(ChatApi::class.java)
        materialApi= retrofit.create(MaterialApi::class.java)
    }
}
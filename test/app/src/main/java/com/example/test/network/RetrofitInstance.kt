package com.example.test.network

import android.content.Context
import com.example.test.network.api.PantryApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.net.Uri

object RetrofitInstance {

    const val BASE_URL = "http://192.168.0.21:8080"

    private lateinit var retrofit: Retrofit
    lateinit var apiService: ApiService
    lateinit var communityApi: CommunityApi
    lateinit var notificationApi: NotificationApi
    lateinit var chatApi: ChatApi
    lateinit var materialApi: MaterialApi
    lateinit var pantryApi: PantryApi

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
        pantryApi= retrofit.create(PantryApi::class.java)
    }
    fun toAbsoluteUrl(pathOrUrl: String?): String? {
        if (pathOrUrl.isNullOrBlank()) return null
        val s = pathOrUrl.trim()
        if (s.startsWith("http://", true) || s.startsWith("https://", true)) return s
        return BASE_URL.trimEnd('/') + "/" + s.trimStart('/')
    }

    /** 아이콘 키/경로를 절대 URL로 변환 */
    fun toIconUrl(iconKeyOrPath: String?): String? {
        if (iconKeyOrPath.isNullOrBlank()) return null
        val s = iconKeyOrPath.trim().removePrefix("icons/")

        // 이미 URL/절대경로면 그대로 절대경로화
        if (s.startsWith("http://", true) || s.startsWith("https://", true) || s.startsWith("/")) {
            return toAbsoluteUrl(s)
        }

        // 키 또는 파일명만 온 경우
        val base = if (s.startsWith("ic_")) "image_" + s.removePrefix("ic_") else s
        val file = if (base.contains('.')) base else "$base.png"
        return toAbsoluteUrl("/icons/$file")
    }

    /** 동영상 파일명/경로 → 절대 URL (`/uploads/videos/{file}` 규칙) */
    fun toVideoUrl(fileNameOrPath: String?): String? {
        if (fileNameOrPath.isNullOrBlank()) return null
        val s = fileNameOrPath.trim()

        // 이미 절대 URL이면 그대로
        if (s.startsWith("http://", true) || s.startsWith("https://", true)) return s

        // 이미 서버 공개 경로로 시작하면 그대로 절대화
        if (s.startsWith("/uploads/")) return toAbsoluteUrl(s)

        // 순수 파일명인 경우 → /uploads/videos/{파일명} 로 조립 (공백/한글 대비 인코딩)
        val encoded = Uri.encode(s)
        return toAbsoluteUrl("/uploads/videos/$encoded")
    }
}
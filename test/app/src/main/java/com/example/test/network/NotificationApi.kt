package com.example.test.network

import com.example.test.model.notification.FcmTokenRequestDTO
import com.example.test.model.notification.NotificationResponseDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface NotificationApi {

    @POST("api/fcm/token")
    fun sendFcmToken(
        @Header("Authorization") token: String,
        @Body request: FcmTokenRequestDTO
    ): Call<Void>

    @POST("api/fcm/test")
    fun sendTestNotification(
        @Header("Authorization") token: String
    ): Call<Void>

    @GET("/api/notifications")
    fun getNotifications(@Header("Authorization") token: String
    ): Call<List<NotificationResponseDTO>>

    //로그아웃 토큰 삭제용
    @POST("api/fcm/token/delete")
    fun deleteFcmToken(
        @Header("Authorization") token: String,
        @Body request: FcmTokenRequestDTO
    ): Call<Void>
}
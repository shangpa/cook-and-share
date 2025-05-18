package com.example.test.network

import com.example.test.model.notification.FcmTokenRequestDTO
import retrofit2.Call
import retrofit2.http.Body
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
}
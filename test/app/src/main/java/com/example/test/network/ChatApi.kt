package com.example.test.network

import com.example.test.model.chat.ChatRoomResponse
import com.example.test.model.chat.UsernameResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ChatApi {
    @POST("/api/chat-room")
    fun createOrGetRoom(
        @Header("Authorization") token: String,
        @Query("postId") postId: Long
    ): Call<ChatRoomResponse>

    @GET("api/user/profile-by-id")
    fun getUserProfileById(
        @Header("Authorization") token: String,
        @Query("id") id: Long
    ): Call<UsernameResponse>
}
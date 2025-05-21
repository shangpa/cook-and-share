package com.example.test.network

import com.example.test.model.chat.ChatMessageDTO
import com.example.test.model.chat.ChatRoomListResponseDTO
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

    @GET("/api/chat-room/list")
    fun getChatRooms(
        @Header("Authorization") token: String
    ): Call<List<ChatRoomListResponseDTO>>

    @GET("/api/chat-message")
    fun getMessages(
        @Header("Authorization") token: String,
        @Query("roomKey") roomKey: String
    ): Call<List<ChatMessageDTO>>
}
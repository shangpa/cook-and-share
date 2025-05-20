package com.example.test.model.chat

data class ChatRoomResponse(
    val roomKey: String,
    val postId: Long,
    val userAId: Long,
    val userBId: Long
)
package com.example.test.model.chat

data class ChatRoomListResponseDTO(
    val roomKey: String,
    val opponentId: Int,
    val opponentUsername: String,
    val lastMessageContent: String,
    val lastMessageTime: String
)
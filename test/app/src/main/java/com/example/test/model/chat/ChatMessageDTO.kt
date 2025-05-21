package com.example.test.model.chat

data class ChatMessageDTO(
    val roomKey: String,
    val message: String,
    val senderId: Long,
    val createdAt: String
)
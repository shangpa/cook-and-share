package com.example.test.model.chat

data class ChatMessage(
    val roomKey: String,
    val senderId: Long,
    val message: String,
    val createdAt: String? = null
)
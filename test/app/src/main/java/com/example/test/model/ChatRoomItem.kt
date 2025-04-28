package com.example.test.model

data class ChatRoomItem(
    val roomId: String,
    val roomName: String,
    var lastMessage: String = "",   // 🔥 추가
    var lastTime: Long = 0L          // 🔥 추가
)

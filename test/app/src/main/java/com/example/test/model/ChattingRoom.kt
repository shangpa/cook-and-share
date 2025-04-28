package com.example.test.model

data class ChattingRoom(
    val roomId: String,
    val roomName: String,
    val participants: List<String> // 참여자 ID 목록
)


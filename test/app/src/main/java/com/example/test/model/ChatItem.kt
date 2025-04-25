package com.example.test.model

data class ChatItem(
    val from: String,     // 보낸 사람
    val content: String,  // 메시지 내용
    val time: String,     // 시간 (ex. "03:01 PM")
    val type: ChatType    // 말풍선 방향 (왼쪽/오른쪽/가운데)
)

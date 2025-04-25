package com.example.test.model

enum class ChatType {
    LEFT_CONTENT,   // 상대방 채팅 → 왼쪽 말풍선
    RIGHT_CONTENT,  // 내 채팅 → 오른쪽 말풍선
    CENTER_CONTENT  // 시스템 메시지 (입장/퇴장 안내) → 가운데
}

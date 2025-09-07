package com.example.test.model.TradePost

data class TradePostUpResult(
    val postId: Long,
    val usedPoints: Int,
    val reason: String,
    val uppedAt: String  // LocalDateTime -> String으로 받는게 안전
)

package com.example.test.model.TradePost

data class TradePostSimpleResponse(
    val tradePostId: Long,
    val title: String,
    val firstImageUrl: String,
    val createdAt: String,
    val price: Int,
    val status: Int
)
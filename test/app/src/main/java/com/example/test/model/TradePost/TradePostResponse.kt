package com.example.test.model.TradePost

data class TradePostResponse(
    val tradePostId: Long,
    val writer: String,
    val category: String,
    val title: String,
    val quantity: Int,
    val price: Int,
    val purchaseDate: String, // yyyy-MM-dd
    val description: String,
    val location: String,
    val imageUrls: String,
    val createdAt: String
)

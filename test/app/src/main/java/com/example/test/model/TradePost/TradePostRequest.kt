package com.example.test.model.TradePost

data class TradePostRequest(
    val category: String,
    val title: String,
    val quantity: Int,
    val price: Int,
    val purchaseDate: String, // "yyyy-MM-dd" 형식
    val description: String,
    val location: String
)

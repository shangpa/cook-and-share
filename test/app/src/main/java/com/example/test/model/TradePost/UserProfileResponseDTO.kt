package com.example.test.model.TradePost

data class UserProfileResponseDTO(
    val username: String,
    val rating: Double,
    val reviewCount: Int,
    val transactionCount: Int
)
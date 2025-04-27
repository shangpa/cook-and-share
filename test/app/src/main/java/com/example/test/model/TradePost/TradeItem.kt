package com.example.test.model.TradePost

data class TradeItem(
    val id: Long,
    val title: String,
    val imageUrl: String,
    val distance: String,
    val date: String,
    val price: String,
    val isCompleted: Boolean
)
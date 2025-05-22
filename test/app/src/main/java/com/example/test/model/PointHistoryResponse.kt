package com.example.test.model

data class PointHistoryResponse(
    val action: String,
    val pointChange: Int,
    val description: String,
    val createdAt: String
)
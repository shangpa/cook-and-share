package com.example.test.model.TradePost

data class TpReviewResponseDTO(
    val tpReviewId: Long,
    val content: String,
    val rating: Int,
    val createdAt: String,
    val username: String
)
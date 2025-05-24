package com.example.test.model.review

data class TpReviewRequestDTO(
    val tradePostId: Long,
    val content: String,
    val rating: Int,
)
data class TpReviewResponseDTO(
    val tpReviewId: Long,
    val content: String,
    val rating: Int,
    val createdAt: String,
    val username: String
)

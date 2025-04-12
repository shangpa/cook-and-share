package com.example.test.model.review

data class ReviewResponseDTO(
    val reviewId: Long,
    val content: String,
    val rating: Int,
    val mediaUrls: String,
    val createdAt: String,
    val username: String
)

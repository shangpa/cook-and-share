package com.example.test.model.review

data class ReviewRequestDTO(
    val recipeId: Long,
    val content: String,
    val rating: Int,
    val mediaUrls: String // 이미지 URL 리스트 (JSON 문자열)
)

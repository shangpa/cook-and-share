package com.example.test.model.community

data class CommunityDetailResponse(
    val id: Long,
    val content: String,
    val writer: String,
    val imageUrls: List<String>,
    val boardType: String,
    val createdAt: String,
    val likeCount: Int,
    val liked: Boolean = false,
    val commentCount: Int = 0
)
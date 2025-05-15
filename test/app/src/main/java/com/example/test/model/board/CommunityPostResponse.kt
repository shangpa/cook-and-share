package com.example.test.model.board

data class CommunityPostResponse(
    val id: Long,
    val content: String,
    val writer: String,
    val imageUrls: List<String>,
    val boardType: String,
    val createdAt: String
)
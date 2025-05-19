package com.example.test.model.community

data class CommunityPostRequest(
    val content: String,
    val imageUrls: List<String>,
    val boardType: String = "FREE"
)
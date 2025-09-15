package com.example.test.model.recipeDetail

data class ShortsSearchItem(
    val id: Long,
    val title: String,
    val thumbnailUrl: String?,
    val authorName: String?,
    val views: Long = 0,
    val createdAt: String? = null
)

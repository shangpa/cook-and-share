package com.example.test.model

data class ShortsCreateRequest(
    val title: String,
    val videoUrl: String,
    val isPublic: Boolean = true
)
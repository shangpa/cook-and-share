package com.example.test.model

import java.io.Serializable

data class ShortObject(
    val id: Int,
    val userId: Int,
    val userName: String,
    val title: String,
    val videoUrl: String,
    val viewCount: Int = 0,
    val likeCount: Int = 0
) : Serializable

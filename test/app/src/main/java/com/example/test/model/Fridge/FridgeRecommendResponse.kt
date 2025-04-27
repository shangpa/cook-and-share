package com.example.test.model.Fridge


data class FridgeRecommendResponse(
    val recipeId    : Long,
    val title: String,
    val mainImageUrl: String,
    val difficulty: String,
    val cookingTime: Int,
    val reviewAverage: Double,
    val reviewCount: Int,
    val writerNickname: String,
    val viewCount: Int,
    val likeCount: Int,
    val createdAt: String
)
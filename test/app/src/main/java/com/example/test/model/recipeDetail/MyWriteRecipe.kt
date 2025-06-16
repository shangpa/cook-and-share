package com.example.test.model.recipeDetail

data class MyWriteRecipe(
    val id: Int,
    val title: String,
    val mainImageUrl: String,
    val heartCount: Int,
    val likeCount: Int,
    val createdAt: String
)

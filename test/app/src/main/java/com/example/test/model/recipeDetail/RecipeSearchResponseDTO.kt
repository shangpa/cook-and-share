package com.example.test.model.recipeDetail

data class RecipeSearchResponseDTO(
    val recipeId: Long,
    val title: String,
    val mainImageUrl: String,
    val difficulty: String,
    val cookingTime: Int,
    val user: Map<String, Any>,
    val category: String,
    val createdAt: String,
    val viewCount: Int,
    val likes: Int,
    val videoUrl: String? = null
)

package com.example.test.model.recipeDetail

data class RecipeMainSearchResponseDTO(
    val recipeId: Long,
    val title: String,
    val mainImageUrl: String?,
    val difficulty: String,
    val cookingTime: Int,
    var liked: Boolean,
    val videoUrl: String?,
    val viewCount: Int,
    val likes: Int,
    val averageRating: Double,
    val reviewCount: Int,
    val user: Map<String, Any>,
    val category: String,
    val createdAt: String
)

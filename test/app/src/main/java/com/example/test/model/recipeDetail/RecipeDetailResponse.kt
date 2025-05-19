package com.example.test.model.recipeDetail

data class RecipeDetailResponse(
    val recipeId: Long,
    val title: String,
    val category: String,
    val ingredients: String,
    val alternativeIngredients: String,
    val handlingMethods: String,
    val cookingSteps: String?,
    val mainImageUrl: String,
    val difficulty: String,
    val tags: String,
    val cookingTime: Int,
    val servings: Int,
    val createdAt: String,
    val isPublic: Boolean,
    val writer: String,
    val videoUrl: String?,
    val viewCount: Int?
)

package com.example.test.model.recipeDetail

data class RecipeDraftDto(
    val recipeId: Long? = null,
    val title: String? = null,
    val category: String? = null,               // "koreaFood" 등
    val ingredients: String? = null,            // JSON string
    val alternativeIngredients: String? = null, // JSON string
    val handlingMethods: String? = null,        // JSON string
    val cookingSteps: String? = null,           // JSON string
    val mainImageUrl: String? = null,
    val difficulty: String? = null,             // "초급","중급","상급"
    val tags: String? = null,
    val cookingTime: Int? = null,
    val servings: Int? = null,
    val createdAt: String? = null,
    val isPublic: Boolean? = null,
    val writer: String? = null,
    val videoUrl: String? = null,
    val recipeType: String? = "IMAGE",
    val isDraft: Boolean? = true
)
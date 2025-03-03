package com.example.test.model

data class RecipeResponse(
    val success: Boolean,
    val message: String,
    val recipeId: Int?
)
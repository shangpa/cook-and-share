package com.example.test.model.recipeDetail

data class CookingStep(
    val step: Int,
    val description: String,
    val mediaUrl: String,
    val mediaType: String,
    val timeInSeconds: Int
)
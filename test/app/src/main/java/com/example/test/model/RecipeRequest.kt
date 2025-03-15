package com.example.test.model

data class RecipeRequest(
    val title: String,
    val category: String,
    val ingredients: String,  // JSON 형식의 재료
    val alternativeIngredients: String, // JSON 형식의 대체 재료
    val handlingMethods: String, //JSON 형식의 처리 방법
    val cookingSteps: String, // JSON 형식의 조리 순서
    val mainImageUrl: String,
    val difficulty: String,
    val tags: String,
    val cookingTime: Int,
    val servings: Int,
    val isPublic: Boolean
)
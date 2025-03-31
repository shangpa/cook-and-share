package com.example.test.model

data class Recipe(
    val recipeId: Long,
    val title: String,
    val mainImageUrl: String,
    val difficulty: String,
    val cookingTime: Int,
    val user: User,
    val tags: String?, // 혹은 List<String> 으로 파싱할 수도 있음
    val createdAt: String?, // 사용 여부에 따라
    val servings: Int?,
    val category: String,
    val ingredients: String?, // JSON 그대로 받을 경우 String
    val alternativeIngredients: String?,
    val handlingMethods: String?,
    val cookingSteps: String?,
    val isPublic: Boolean
)

data class User(
    val id: Int,
    val name: String
)

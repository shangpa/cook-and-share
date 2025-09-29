package com.example.test.model

data class RecipeRequest(
    val title: String,
    val category: String,
    val ingredients: List<MasterIngredient>,
    val alternativeIngredients: String, // JSON 형식의 대체 재료
    val handlingMethods: String, //JSON 형식의 처리 방법
    val cookingSteps: String, // JSON 형식의 조리 순서
    val mainImageUrl: String,
    val difficulty: String,
    val tags: String,
    val cookingTime: Int,
    val servings: Int,
    val isPublic: Boolean,
    val videoUrl: String = "",
    val isDraft: Boolean = false,   // 기본값 false, 임시 저장이면 true
    val recipeType: String = "IMAGE" //Image, Video, Both 셋중하나
)
package com.example.test.Utils

import com.example.test.model.Recipe
import com.example.test.model.User
import com.example.test.model.recipeDetail.RecipeSearchResponseDTO

// 단건 변환
fun RecipeSearchResponseDTO.toUiRecipe(): Recipe {
    val uid = (user["id"] as? Number)?.toInt() ?: 0
    val uname = (user["name"] as? String) ?: ""
    return Recipe(
        recipeId = recipeId,
        title = title,
        mainImageUrl = mainImageUrl, // 절대/상대 경로 보정은 Glide쪽에서 처리
        difficulty = difficulty,
        cookingTime = cookingTime,
        user = User(id = uid, name = uname),
        tags = null,
        createdAt = createdAt,
        servings = null,
        category = category,
        ingredients = null,
        alternativeIngredients = null,
        handlingMethods = null,
        cookingSteps = null,
        isPublic = true,
        videoUrl = videoUrl,
        averageRating = 0.0, // DTO에 없음 → 기본값
        reviewCount = 0,     // DTO에 없음 → 기본값
        liked = false        // DTO에 없음 → 기본값
    )
}

// 리스트 변환
fun List<RecipeSearchResponseDTO>.toUiRecipes(): List<Recipe> = map { it.toUiRecipe() }

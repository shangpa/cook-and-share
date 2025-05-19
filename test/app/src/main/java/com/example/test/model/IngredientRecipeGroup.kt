package com.example.test.model

data class IngredientRecipeGroup(
    val ingredient: String,
    val recipes: List<RecipeSearchResponseDTO>
)

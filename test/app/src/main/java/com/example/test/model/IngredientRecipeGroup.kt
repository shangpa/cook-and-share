package com.example.test.model

import com.example.test.model.recipeDetail.RecipeSearchResponseDTO

data class IngredientRecipeGroup(
    val ingredient: String,
    val recipes: List<RecipeSearchResponseDTO>
)

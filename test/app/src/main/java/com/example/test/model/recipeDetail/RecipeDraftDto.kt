package com.example.test.model.recipeDetail

import com.google.gson.annotations.SerializedName

data class RecipeIngredientRes(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: Double
)

data class RecipeDraftDto(
    val recipeId: Long?,
    val title: String?,
    val category: String?,
    @SerializedName("ingredients") val ingredients: List<RecipeIngredientRes>?, // ← Res로!
    val alternativeIngredients: String?,
    val handlingMethods: String?,
    val cookingSteps: String?,
    val mainImageUrl: String?,
    val difficulty: String?,
    val tags: String?,
    val cookingTime: Int?,
    val servings: Int?,
    @SerializedName("isPublic") val isPublic: Boolean?,
    val videoUrl: String?,
    val recipeType: String?,
    @SerializedName("isDraft") val isDraft: Boolean?,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class RecipeIngredientReq(
    @SerializedName("id") val id: Long?,
    @SerializedName("amount") val quantity: Double?,   // ← 서버 필드명 amount로 매핑
    // 서버는 unit을 받지 않으니 보내지 않아도 됨
)

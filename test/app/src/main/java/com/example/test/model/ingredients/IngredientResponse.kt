package com.example.test.model.ingredients

data class IngredientResponse(
    val id: Long,
    val nameKo: String,
    val category: String,
    val defaultUnitId: Long?,
    val iconUrl: String
)

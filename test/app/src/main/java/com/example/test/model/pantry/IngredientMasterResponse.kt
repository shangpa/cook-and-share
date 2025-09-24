package com.example.test.model.pantry

data class IngredientMasterResponse(
    val id: Long,
    val nameKo: String,
    val category: String,
    val defaultUnitId: Long?,
    val iconUrl: String?
)
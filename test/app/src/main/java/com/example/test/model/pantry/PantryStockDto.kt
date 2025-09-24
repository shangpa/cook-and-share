package com.example.test.model.pantry

data class PantryStockDto(
    val id: Long,
    val ingredientId: Long?,
    val unitId: Long?,
    val ingredientName: String,
    val category: String,
    val storage: String,
    val quantity: String,
    val unitName: String,
    val iconUrl: String?,
    val purchasedAt: String?,
    val expiresAt: String?
)


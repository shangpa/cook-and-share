package com.example.test.model.pantry

data class PantryStockDetailResponse(
    val id: Long,
    val ingredientName: String,
    val category: String,
    val storage: String,
    val quantity: String,
    val unitName: String,
    val iconUrl: String?,
    val purchasedAt: String?,   // yyyy-MM-dd
    val expiresAt: String?      // yyyy-MM-dd  ⬅⬅ 추가
)


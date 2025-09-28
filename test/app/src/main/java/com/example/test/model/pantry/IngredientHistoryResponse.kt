package com.example.test.model.pantry

data class IngredientHistoryResponse(
    val id: Long,
    val action: String,          // "ADD" | "USE" | "DISCARD" | "ADJUST"
    val ingredientId: Long,
    val ingredientName: String,
    val category: String?,
    val quantity: String,        // "500.000" 혹은 "-3.000"
    val unitId: Long,
    val unitName: String,
    val purchasedAt: String?,    // yyyy-MM-dd
    val expiresAt: String?,      // yyyy-MM-dd
    val createdAt: String,        // yyyy-MM-dd HH:mm:ss
    val iconUrl: String?
)

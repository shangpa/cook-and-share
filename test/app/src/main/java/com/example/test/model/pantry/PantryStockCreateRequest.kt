package com.example.test.model.pantry

data class PantryStockCreateRequest(
    val ingredientId: Long,
    val unitId: Long,
    val quantity: String,
    val storage: String,      // FRIDGE/FREEZER/PANTRY
    val purchasedAt: String?, // yyyy-MM-dd
    val expiresAt: String?,   // yyyy-MM-dd
    val memo: String? = null,
    val source: String? = "MANUAL"
)
package com.example.test.model.pantry

data class PantryStockUpdateRequest(
    val quantity: String? = null,
    val storage: String? = null,
    val purchasedAt: String? = null,
    val expiresAt: String? = null      // ⬅⬅ 추가
)
package com.example.test.model.Fridge

data class FridgeResponse(
    val id: Long,
    val ingredientName: String,
    val storageArea: String,
    val fridgeDate: String,
    val dateOption: String?,
    val quantity: Double,
    val price: Double,
    val unitCategory: String,
    val unitDetail: String,
    val updatedAt: String,
    val userId: Long
)


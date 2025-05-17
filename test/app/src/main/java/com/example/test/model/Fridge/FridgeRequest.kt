package com.example.test.model.Fridge

data class FridgeRequest(
    val ingredientName: String,
    val storageArea: String,
    val fridgeDate: String,
    val dateOption: String,
    val quantity: Double,
    val unitCategory: String,
    val unitDetail: String,
    val userId: Long
)

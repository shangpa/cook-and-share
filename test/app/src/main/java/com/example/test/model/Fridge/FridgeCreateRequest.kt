package com.example.test.model.Fridge

data class FridgeCreateRequest(
    val ingredientName: String,
    val quantity: Double,
    val fridgeDate: String, // yyyy-MM-dd
    val dateOption: String = "구매일자",
    val storageArea: String = "실온",
    val unitDetail: String = "개",
    val unitCategory: String = "COUNT"
)

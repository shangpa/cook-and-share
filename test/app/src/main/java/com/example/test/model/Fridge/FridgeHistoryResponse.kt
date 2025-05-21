package com.example.test.model.Fridge

data class FridgeHistoryResponse(
    val ingredientName: String,
    val quantity: Double,
    val actionType: String, // "ADD" or "USE"
    val actionDate: String  // 예: "2025-05-21T13:14:00"
)

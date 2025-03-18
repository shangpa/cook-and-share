package com.example.test.model

// FridgeRequest.kt
data class FridgeRequest(
    val ingredientName: String,
    val storageArea: String,
    val fridgeDate: String, // "YYYY-MM-DD" 형식
    val dateOption: String?,
    val quantity: Double,
    val price: Double,
    val unitCategory: String, // 예: "WEIGHT", "VOLUME", "COUNT"
    val unitDetail: String,
    val userId: Long  // 로그인한 사용자 ID
)

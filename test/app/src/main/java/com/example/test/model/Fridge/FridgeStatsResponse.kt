package com.example.test.model.Fridge

data class FridgeStatsResponse(
    val nowCount: Long,
    val eatenCount: Long,
    val expiredCount: Long,
    val refrigeratedCount: Long,
    val frozenCount: Long,
    val roomCount: Long
)
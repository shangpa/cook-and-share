package com.example.test.model.pantry

data class PantryStatsResponse(
    val nowCount: Long,
    val eatenCount: Long,
    val expiredCount: Long,
    val categoryCounts: Map<String, Long>,
    val topConsumedName: String?
)
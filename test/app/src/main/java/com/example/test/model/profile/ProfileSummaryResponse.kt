package com.example.test.model.profile

data class ProfileSummaryResponse(
    val userId: Long,
    val nickname: String,
    val recipeCount: Long,
    val shortsCount: Long,
    val followersCount: Long,
    val followingCount: Long,
    val following: Boolean,
    val mine: Boolean
)

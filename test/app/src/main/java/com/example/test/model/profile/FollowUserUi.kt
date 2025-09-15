package com.example.test.model.profile

data class FollowUserUi(
    val userId: Int,
    val name: String,
    val username: String,
    val profileImageUrl: String?,
    val isFollowingByMe: Boolean,
    val isFollowingMe: Boolean
)

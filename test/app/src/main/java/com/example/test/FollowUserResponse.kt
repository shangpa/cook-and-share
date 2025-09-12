package com.example.test

data class FollowUserResponse(
    val userId: Int,
    val nickname: String,
    val handle: String,
    val profileImageUrl: String? = null,
    val followingByMe: Boolean, // 내가 이 사람 팔로우 중?
    val followingMe: Boolean    // 그 사람이 나를 팔로우 중?
)

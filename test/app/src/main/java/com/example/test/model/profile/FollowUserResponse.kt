package com.example.test.model.profile

data class FollowUserResponse(
    val userId: Int,
    val name: String?,              // 서버에서 null 가능성 대비
    val username: String?,          // 서버에서 null 가능성 대비
    val profileImageUrl: String?,
    val followingByMe: Boolean,
    val followingMe: Boolean
)
package com.example.test

data class FollowUserUi(
    val userId: Int,
    val nickname: String,
    val handle: String,
    val profileImageUrl: String? = null,
    var isFollowingByMe: Boolean, // 내가 이 사람을 팔로우 중?
    var isFollowingMe: Boolean    // 그 사람이 나를 팔로우 중? (팔로워 탭 표시용)
)

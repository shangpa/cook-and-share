package com.example.test.model.board

data class CommunityMainResponse(
    val popularBoards: List<CommunityDetailResponse>,
    val freeBoards: List<CommunityDetailResponse>,
    val cookBoards: List<CommunityDetailResponse>
)
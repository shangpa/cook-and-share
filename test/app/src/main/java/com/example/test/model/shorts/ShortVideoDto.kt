package com.example.test.model.shorts

data class ShortVideoDto(
    val id: Long,
    val title: String,
    val thumbnailUrl: String?,
    val viewCount: Int,
    val videoUrl: String?,
    val likeCount: Int,
    val createdAt: String?
)

data class ShortVideoListResponse(
    val videos: List<ShortVideoDto>
)

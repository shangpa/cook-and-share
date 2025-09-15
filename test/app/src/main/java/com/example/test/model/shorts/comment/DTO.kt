package com.example.test.model.shorts.comment

data class ShortsCommentRequestDTO(
    val content: String
)

data class ShortsCommentResponseDTO(
    val id: Long,
    val user: String,
    val content: String,
    val createdAt: String
)

data class ShortsCommentListWithCountDTO(
    val comments: List<ShortsCommentResponseDTO>,
    val count: Int
)

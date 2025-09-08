package com.example.test.model.shorts

data class CommentRequestDTO(
    val content: String
)

data class ShortCommentResponse(
    val id: Long,
    val content: String,
    val createdAt: String,
    val username: String // 👈 user.username 대신 바로 username
)
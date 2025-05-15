package com.example.test.model.board

data class CommentResponse(
    val user: String,
    val content: String,
    val createdAt: String
)
data class CommentListResponse(
    val comments: List<CommentResponse>,
    val count: Int
)
data class CommentRequest(
    val content: String
)
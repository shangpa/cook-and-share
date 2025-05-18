package com.example.test.model.notification

data class NotificationResponseDTO(
    val category: String,
    val content: String,
    val createdAt: String,
    val read: Boolean
)
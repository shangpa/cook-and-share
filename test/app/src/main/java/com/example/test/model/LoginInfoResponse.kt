package com.example.test.model

data class LoginInfoResponse(
    val id: Long,
    val userName: String,
    val name: String,
    val profileImageUrl: String? = null
)
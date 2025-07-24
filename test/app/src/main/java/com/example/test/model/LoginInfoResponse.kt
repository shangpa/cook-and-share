package com.example.test.model

data class LoginInfoResponse(
    val userName: String,
    val name: String,
    val profileImageUrl: String? = null
)
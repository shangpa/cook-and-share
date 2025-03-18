package com.example.test.model

data class LoginResponse(
    val message: String,
    val token: String,
    val userId: Long
)

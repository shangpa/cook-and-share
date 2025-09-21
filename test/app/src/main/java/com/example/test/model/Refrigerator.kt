package com.example.test.model.refrigerator

data class Refrigerator(
    val id: Long,
    val name: String,
    val memo: String? = null,
    val imageUrl: String? = null
)

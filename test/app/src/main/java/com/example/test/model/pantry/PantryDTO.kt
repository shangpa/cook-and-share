package com.example.test.model.pantry

data class PantryCreateRequest(
    val name: String,
    val note: String? = null,
    val imageUrl: String? = null,
    val isDefault: Boolean? = null,
    val sortOrder: Int? = 0
)

data class PantryUpdateRequest(
    val name: String? = null,
    val note: String? = null,
    val imageUrl: String? = null,
    val isDefault: Boolean? = null,
    val sortOrder: Int? = null
)
data class PantryResponse(
    val id: Long,
    val name: String,
    val note: String?,
    val imageUrl: String?,
    val isDefault: Boolean,
    val sortOrder: Int,
    val createdAt: String
)
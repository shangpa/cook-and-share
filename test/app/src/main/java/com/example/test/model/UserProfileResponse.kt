package com.example.test.model

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    val id: Long,
    @SerializedName("username") val username: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("profileImageUrl") val profileImageUrl: String?
)

package com.example.test.model

import com.google.gson.annotations.SerializedName

data class CookingStep(
    @SerializedName("step") val step: Int,
    @SerializedName("description") val description: String,
    @SerializedName("mediaUrl") val mediaUrl: String,
    @SerializedName("mediaType") val mediaType: String,
    @SerializedName("timeInSeconds") val timeInSeconds: Int // 추가된 필드

)
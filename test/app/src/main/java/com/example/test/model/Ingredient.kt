package com.example.test.model

import com.google.gson.annotations.SerializedName

data class Ingredient(
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: String
)
data class MasterIngredient(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: String
)

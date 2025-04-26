package com.example.test.model.Fridge

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedIngredient(
    val name: String,
    val quantity: Double?,
    val unit: String,
    val dateLabel: String,
    val dateText: String
) : Parcelable
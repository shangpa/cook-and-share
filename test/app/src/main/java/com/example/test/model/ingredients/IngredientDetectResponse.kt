package com.example.test.model.ingredients

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IngredientDetectResponse(
    val ingredientId: Long?,
    val nameKo: String,
    val iconUrl: String?
) : Parcelable

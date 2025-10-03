package com.example.test.model.recipt

data class OcrItemUi(
    val checked: Boolean,
    val nameRaw: String,
    val quantityStr: String,
    val ingredientId: Long?,
    val unitId: Long?,
    val unitName: String?,
    val matchedName: String?
)

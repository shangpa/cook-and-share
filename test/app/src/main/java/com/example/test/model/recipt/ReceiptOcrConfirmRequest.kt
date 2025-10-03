package com.example.test.model.recipt

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReceiptOcrConfirmRequest(
    val items: List<Item>
) : Parcelable {
    @Parcelize
    data class Item(
        val nameRaw: String,
        val ingredientId: Long?,   // 사용자가 매칭한 재료 ID(없어도 됨)
        val quantity: String,      // "2.0" 등 문자열
        val unitId: Long?,         // null이면 서버에서 기본단위
        val storage: String,       // FRIDGE/FREEZER/PANTRY
        val purchasedAt: String?,  // yyyy-MM-dd
        val expiresAt: String?     // yyyy-MM-dd
    ) : Parcelable
}
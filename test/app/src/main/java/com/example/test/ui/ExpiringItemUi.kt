package com.example.test.ui

import com.example.test.model.pantry.PantryStockDto
import com.example.test.model.recipeDetail.RecipeMainSearchResponseDTO

/**
 * 메인 '임박 재료' 섹션의 화면 모델.
 * 어댑터(ExpiringIngredientAdapter)에서 기대하는 필드명에 정확히 맞춤.
 */
data class ExpiringItemUi(
    val pantryId: Long,
    val pantryName: String,

    val stockId: Long?,
    val ingredientName: String,

    // 어댑터에서 tvQty, tvUnit에 넣음
    val quantityText: String,           // 숫자 부분만 (예: "500" / "2" / "1.5")
    val unitName: String?,              // 단위 (예: "g" / "개" / null)

    val storage: String?,
    val purchasedAt: String?,           // yyyy-MM-dd
    val expiresAt: String?,             // yyyy-MM-dd
    val expiresAtText: String?,         // 표시용 텍스트(그대로 사용)
    val iconUrl: String?,

    val recipes: List<RecipeMainSearchResponseDTO>
)

/**
 * PantryStockDto -> ExpiringItemUi
 * - quantity는 "123.000" 같은 포맷이 올 수 있어 소수점 0 제거해서 깔끔히 표시
 * - unitName 그대로 전달
 * - expiresAtText는 서버값 그대로 사용 (null이면 어댑터에서 "-"로 표시)
 */
fun PantryStockDto.toExpiringUi(
    pantryName: String,
    pantryId: Long,
    recipes: List<RecipeMainSearchResponseDTO>
): ExpiringItemUi {

    val qtyRaw = (this.quantity ?: "").trim()
    // "123.000" -> "123", "1.500" -> "1.5"
    val quantityOnly = runCatching {
        if (qtyRaw.isEmpty()) "" else {
            val bd = java.math.BigDecimal(qtyRaw).stripTrailingZeros()
            bd.toPlainString()
        }
    }.getOrElse { qtyRaw }

    return ExpiringItemUi(
        pantryId = pantryId,
        pantryName = pantryName,

        stockId = this.id,
        ingredientName = this.ingredientName,

        quantityText = quantityOnly,     // 숫자만
        unitName = this.unitName,        // 단위

        storage = this.storage,
        purchasedAt = this.purchasedAt,
        expiresAt = this.expiresAt,
        expiresAtText = this.expiresAt,  // 어댑터가 그대로 표시

        iconUrl = this.iconUrl,
        recipes = recipes
    )
}

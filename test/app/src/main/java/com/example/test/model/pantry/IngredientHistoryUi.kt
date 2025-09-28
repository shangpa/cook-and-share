package com.example.test.model.pantry

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.math.BigDecimal

enum class HistoryType {
    ADD,        // 추가 (항상 +)
    USE,        // 사용 (항상 -)  ← 필요 시 별도 엔드포인트
    DISCARD,    // 폐기 (항상 -)
    ADJUST      // 조정 (± 부호 그대로)
}

data class IngredientHistoryUi(
    val id: Long,
    val type: HistoryType,          // ADD / USE / DISCARD / ADJUST
    val ingredientName: String,
    val amountText: String,         // 항상 절대값 표시: "500 g" / "2 개"
    val purchasedAt: LocalDate?,
    val expiresAt: LocalDate?,
    val createdAt: LocalDateTime,
    val iconUrl: String?,
    val isIncrease: Boolean         // 표시용 부호/색 결정 (true=+파랑, false=−빨강)
)

private val DF_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val DF_DT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private val DOT = DateTimeFormatter.ofPattern("yyyy.MM.dd")

fun LocalDate?.toDot(): String? =
    this?.format(DOT)

fun String?.toLocalDateOrNull(): LocalDate? =
    if (this.isNullOrBlank()) null else runCatching { LocalDate.parse(this, DF_DATE) }.getOrNull()

fun String.toLocalDateTimeOrNow(): LocalDateTime =
    runCatching { LocalDateTime.parse(this, DF_DT) }.getOrElse { LocalDateTime.now() }

private fun String.trimQty(): String = runCatching {
    toBigDecimal().stripTrailingZeros().toPlainString()
}.getOrElse { this }

/** 서버 응답 → 화면용 모델 */
fun IngredientHistoryResponse.toUi(): IngredientHistoryUi {
    // changeQty 문자열 → BigDecimal (ADJUST는 ±가 들어올 수 있음)
    val bd = runCatching { quantity.toBigDecimal() }.getOrElse { BigDecimal.ZERO }

    // 숫자 표시는 항상 절대값
    val qtyAbs = bd.abs().stripTrailingZeros().toPlainString()
    val amount = if (unitName.isNullOrBlank()) qtyAbs else "$qtyAbs $unitName"

    // 액션 → 타입
    val type = when (action.uppercase()) {
        "ADD" -> HistoryType.ADD
        "USE" -> HistoryType.USE
        "DISCARD" -> HistoryType.DISCARD
        "ADJUST" -> HistoryType.ADJUST
        else -> HistoryType.ADJUST
    }

    // 표시 부호/색 결정
    //  - ADD: 항상 증가(+)
    //  - USE/DISCARD: 항상 감소(−)
    //  - ADJUST: changeQty 부호를 따른다
    val isIncrease = when (type) {
        HistoryType.ADD -> true
        HistoryType.USE, HistoryType.DISCARD -> false
        HistoryType.ADJUST -> bd.signum() >= 0
    }

    return IngredientHistoryUi(
        id = id,
        type = type,
        ingredientName = ingredientName,
        amountText = amount,                         // 절대값만
        purchasedAt = purchasedAt.toLocalDateOrNull(),
        expiresAt = expiresAt.toLocalDateOrNull(),
        createdAt = createdAt.toLocalDateTimeOrNow(),
        iconUrl = iconUrl,
        isIncrease = isIncrease
    )
}

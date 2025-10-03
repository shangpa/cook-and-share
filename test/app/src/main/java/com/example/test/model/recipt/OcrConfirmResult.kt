package com.example.test.model.recipt

data class OcrConfirmResult(
    val nameRaw: String,   // 요청에 담겼던 원본 이름
    val status: String,    // "OK" or "ERROR"
    val stockId: Long?,    // 성공 시 생성/업서트된 재고 id
    val error: String?     // 실패 시 에러 메시지
)
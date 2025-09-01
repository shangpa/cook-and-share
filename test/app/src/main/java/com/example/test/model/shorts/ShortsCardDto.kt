package com.example.test.model.shorts

//레시피 탭 - 1분 레시피 카드용
data class ShortsCardDto(
    val id: Long,
    val title: String,
    val thumbnailUrl: String? = null
)
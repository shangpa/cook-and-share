package com.example.test.model.recipt

data class VisionRequest(
    val requests: List<RequestItem>
)

data class RequestItem(
    val image: Image,
    val features: List<Feature>
)

data class Image(
    val content: String
)

data class Feature(
    val type: String = "TEXT_DETECTION",
    val maxResults: Int = 1
)
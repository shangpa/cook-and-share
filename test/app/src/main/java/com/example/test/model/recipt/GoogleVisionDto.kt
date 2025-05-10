package com.example.test.model.recipt

data class VisionRequest(
    val requests: List<RequestItem>
)

data class RequestItem(
    val image: Image,
    val features: List<Feature>,
    val imageContext: ImageContext
)

data class Image(
    val content: String
)

data class Feature(
    val type: String = "DOCUMENT_TEXT_DETECTION",
    val maxResults: Int = 1
)
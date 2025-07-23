// ShortObject.kt
package com.example.test

import java.io.Serializable

data class ShortObject(
    val id: Int,
    val userName: String,
    val contents: String,
    val url: String
) : Serializable

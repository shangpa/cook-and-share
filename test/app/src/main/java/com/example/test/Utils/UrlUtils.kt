package com.example.test.Utils

import android.net.Uri
import com.example.test.network.RetrofitInstance

object UrlUtils {
    fun resolveToBase(relativeOrAbsolute: String?): String? {
        if (relativeOrAbsolute.isNullOrBlank()) return null
        val base = RetrofitInstance.BASE_URL.trimEnd('/')

        return try {
            val uri = Uri.parse(relativeOrAbsolute)
            if (uri.scheme.isNullOrBlank()) {
                // "/uploads/..." 형태
                "$base/${relativeOrAbsolute.trimStart('/')}"
            } else {
                // 절대 URL → path만 가져와서 BASE_URL로 교체
                val path = uri.path ?: return relativeOrAbsolute
                "$base/${path.trimStart('/')}"
            }
        } catch (_: Throwable) {
            "$base/${relativeOrAbsolute.trimStart('/')}"
        }
    }
}

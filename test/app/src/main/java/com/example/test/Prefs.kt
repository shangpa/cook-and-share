package com.example.test

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var userId: String?
        get() = prefs.getString("userId", null)
        set(value) = prefs.edit().putString("userId", value).apply()

    var userName: String?
        get() = prefs.getString("userName", null)
        set(value) = prefs.edit().putString("userName", value).apply()
}


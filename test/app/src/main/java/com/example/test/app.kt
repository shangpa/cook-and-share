package com.example.test

import Prefs
import android.app.Application
import android.content.Context
import com.example.test.network.RetrofitInstance

class App : Application() {
    companion object {
        lateinit var context: Context
        lateinit var prefs: Prefs
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        prefs = Prefs(applicationContext)
        // Retrofit 안전하게 초기화!
        RetrofitInstance.init(context)
    }
}


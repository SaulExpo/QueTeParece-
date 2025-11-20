package com.exmosaul.queteparece

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        LanguageManager.init(this)
    }
}
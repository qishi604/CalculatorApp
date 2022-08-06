package com.lanayru.caculator

import android.app.Application

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        VibrateUtils.setup(this)
    }
}
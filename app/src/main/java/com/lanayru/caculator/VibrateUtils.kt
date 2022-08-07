package com.lanayru.caculator

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object VibrateUtils {

    lateinit var _context: Application

    fun setup(app: Application) {
        _context = app
    }

    fun vibrate() {
        (_context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)?.let { vibrator ->
            val mills = 10L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                vibrator.vibrate(effect)
            } else {
                vibrator.vibrate(mills)
            }
        }
    }
}
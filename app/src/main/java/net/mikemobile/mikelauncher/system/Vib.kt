package net.mikemobile.mikelauncher.system

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

fun triggerVibration(context: Context, milliseconds: Long = 30L) {

    val vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // For API level 26 and above
        val vibrationEffect = VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    } else {
        // For below API level 26
        vibrator.vibrate(milliseconds)  // Vibrate for 500 milliseconds
    }
}
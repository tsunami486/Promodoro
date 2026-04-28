package com.example.promodoro.utils

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object AlarmUtils {

    fun playAlarmAndVibrate(context: Context){
        //铃声
//        try {
//            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//            val ringtone = RingtoneManager.getRingtone(context,uri)
//            ringtone.play()
//        } catch (e: Exception){
//            e.printStackTrace()
//        }

        //震动
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }

    }
}
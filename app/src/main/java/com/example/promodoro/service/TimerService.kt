package com.example.promodoro.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.promodoro.MainActivity
import com.example.promodoro.R
import com.example.promodoro.utils.TimeUtils
import kotlinx.coroutines.*

class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "TimerChannel"
        const val NOTIFICATION_ID = 1

        const val ACTION_START_OR_UPDATE = "ACTION_START_OR_UPDATE"
        const val ACTION_STOP = "ACTION_STOP"

        const val EXTRA_TIME = "EXTRA_TIME"
        const val EXTRA_IS_BREAK = "EXTRA_IS_BREAK"
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_OR_UPDATE -> {
                val targetTimeMillis = intent.getLongExtra(EXTRA_TIME, 0L)
                val isBreakMode = intent.getBooleanExtra(EXTRA_IS_BREAK, false)
                startManualCountdown(targetTimeMillis, isBreakMode)
            }
            ACTION_STOP -> {
                timerJob?.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startManualCountdown(targetTimeMillis: Long, isBreakMode: Boolean) {
        timerJob?.cancel()

        val initialRemainingSeconds = ((targetTimeMillis - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)

        val initialNotification = buildNotification(initialRemainingSeconds, isBreakMode)
        startForeground(NOTIFICATION_ID, initialNotification)

        timerJob = serviceScope.launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val remainingSeconds = ((targetTimeMillis - currentTime) / 1000).toInt()

                if (remainingSeconds <= 0) {
                    val finalNotification = buildNotification(0, isBreakMode)
                    notificationManager.notify(NOTIFICATION_ID, finalNotification)
                    break
                }

                // 强制更新通知栏
                val notification = buildNotification(remainingSeconds, isBreakMode)
                notificationManager.notify(NOTIFICATION_ID, notification)

                delay(1000L)
            }
        }
    }

    private fun buildNotification(remainingSeconds: Int, isBreakMode: Boolean): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (isBreakMode) "休息中" else "保持专注"
        val timeString = TimeUtils.formatTime(remainingSeconds)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("剩余时间: $timeString")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "番茄钟倒计时",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
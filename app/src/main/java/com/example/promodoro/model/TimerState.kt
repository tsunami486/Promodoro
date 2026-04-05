package com.example.promodoro.model

data class TimerState(
    val timeRemaining: Int = 25*60,
    val timeToRemaining: Int = 25*60,
    val isRunning: Boolean = false,
    val isBreak: Boolean = false,
    val isImmersiveModeEnabled: Boolean = true
)

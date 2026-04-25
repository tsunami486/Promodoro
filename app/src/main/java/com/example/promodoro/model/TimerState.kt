package com.example.promodoro.model

import com.google.android.material.color.utilities.DynamicColor

data class TimerState(
    val focusTimeLength: Int = 25*60,
    val breakTimeLength:Int = 5*60,
    val timeRemaining: Int = 1*60,
    val isRunning: Boolean = false,
    val isBreak: Boolean = false,
    val isImmersiveModeEnabled: Boolean = false,
    val isFocusModeEnabled: Boolean = false,
    val isFocusFailed: Boolean = false,
    val isBreakMode: Boolean = false,
    val isDynamicColorEnabled: Boolean = true,
    val isAodModeEnabled: Boolean = true,
    val alarmTrigger: Int = 0
)

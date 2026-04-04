package com.example.promodoro.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 浅色模式配色方案
private val LightColorScheme = lightColorScheme(
    primary = PomodoroRed,          // 主要组件颜色
    secondary = BreakGreen,         // 次要颜色
    background = LightBackground,   // 屏幕背景色
    onPrimary = TextPrimaryLight,   // 在主色调上的文字颜色
    onBackground = TextPrimaryDark  // 在背景上的文字颜色
)

// 深色模式配色方案
private val DarkColorScheme = darkColorScheme(
    primary = PomodoroRedDark,
    secondary = BreakGreenDark,
    background = DarkBackground,
    onPrimary = TextPrimaryLight,
    onBackground = TextPrimaryLight
)

@Composable
fun PomodoroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 获取当前视图，用于设置状态栏颜色
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 让手机顶部的状态栏颜色与应用主色调一致
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
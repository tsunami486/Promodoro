package com.example.promodoro.ui.screens

import com.example.promodoro.ui.components.AnimatedCircularTimer
import android.app.Activity
import android.content.Intent
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.promodoro.model.TimerState
import com.example.promodoro.service.TimerService
import com.example.promodoro.viewmodel.TimerViewModel
import com.example.promodoro.ui.theme.PomodoroTheme
import kotlinx.coroutines.launch
import android.app.NotificationManager
import android.content.Context
import com.example.promodoro.utils.AlarmUtils


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = viewModel(),
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var isAodActive by remember { mutableStateOf(false) }

    LaunchedEffect(state.isRunning) {
//        if (state.focusTimeLength == state.timeRemaining){
//        if (state.isRunning && state.isAodModeEnabled) isAodActive = true
//        }else if (!state.isRunning) {
//            isAodActive = false
//        }

        if(!state.isRunning){
            isAodActive = false
        }
    }

    LaunchedEffect(state.isRunning) {
        if (state.isRunning && !state.isImmersiveModeEnabled && !state.isFocusModeEnabled) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = TimerService.ACTION_START_OR_UPDATE
                val targetTimeMillis = System.currentTimeMillis() + state.timeRemaining * 1000L
                putExtra(TimerService.EXTRA_TIME, targetTimeMillis)
                putExtra(TimerService.EXTRA_IS_BREAK, state.isBreakMode)
            }
            ContextCompat.startForegroundService(context, intent)
        } else {
            val intent = Intent(context, TimerService::class.java).apply {
                action = TimerService.ACTION_STOP
            }
            context.startService(intent)
        }
    }

    LaunchedEffect(state.alarmTrigger) {
        if (state.alarmTrigger > 0 && state.timeRemaining == 0) {
            AlarmUtils.playAlarmAndVibrate(context)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.handleAppBackground()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                if (state.isRunning && !state.isImmersiveModeEnabled && !state.isFocusModeEnabled) {
                    val intent = Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_START_OR_UPDATE
                        val targetTimeMillis = System.currentTimeMillis() + state.timeRemaining * 1000L
                        putExtra(TimerService.EXTRA_TIME, targetTimeMillis)
                        putExtra(TimerService.EXTRA_IS_BREAK, state.isBreakMode)
                    }
                    ContextCompat.startForegroundService(context, intent)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (state.isFocusFailed && state.isImmersiveModeEnabled) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("专注失败") },
            text = { Text("由于你离开了，专注已中断。下次请保持专注哦！") },
            confirmButton = {
                Button(onClick = { viewModel.clearFocusFailure() }) { Text("我知道了") }
            }
        )
    }

    // ====== 系统全屏与屏幕常亮接管 ======
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        val insetsController = remember { WindowCompat.getInsetsController(window, view) }

        LaunchedEffect(state.isRunning, state.isImmersiveModeEnabled, isAodActive) {
            if (isAodActive) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                if (state.isRunning && state.isImmersiveModeEnabled) {
                    insetsController.hide(WindowInsetsCompat.Type.systemBars())
                    insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }

    TimerScreenContent(
        modifier = modifier,
        state = state,
        isAodActive = isAodActive,
        onAodToggle = {
            if (state.isRunning && state.isAodModeEnabled) {
                isAodActive = !isAodActive
            }
        },
        onToggleClick = { viewModel.toggleTimer() },
        onResetClick = { viewModel.resetTimer() },
        onOpenSettings = onNavigateToSettings,
        onTimeTextClick = { if (!state.isRunning) showSheet = true }
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            SettingsSheetContent(
                currentBreakMinutes = state.breakTimeLength / 60,
                currentFocusMinutes = state.focusTimeLength / 60,
                onSave = { newFocusMinutes, newBreakMinutes ->
                    viewModel.updateTimeSettings(newFocusMinutes, newBreakMinutes)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showSheet = false
                    }
                }
            )
        }
    }
}

@Composable
fun TimerScreenContent(
    modifier: Modifier = Modifier,
    state: TimerState,
    isAodActive: Boolean,
    onAodToggle: () -> Unit,
    onToggleClick: () -> Unit,
    onResetClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onTimeTextClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isAodActive) Color.Black else MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onAodToggle
            )
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = !isAodActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text =if (state.isRunning){
                        if (state.isBreakMode)"休息一下吧" else "专注中"
                    }else{""},
                    style = MaterialTheme.typography.titleLarge,
                    color = if (state.isBreakMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!state.isRunning) {
                            onTimeTextClick()
                        } else {
                            onAodToggle()
                        }
                    }
                    .padding(16.dp)
            ) {
                val totalTimeForCurrentMode = if (state.isBreakMode) state.breakTimeLength else state.focusTimeLength

                AnimatedCircularTimer(
                    timeRemaining = state.timeRemaining,
                    totalTime = totalTimeForCurrentMode,
                    isRunning = state.isRunning,
                    isBreakMode = state.isBreakMode,
                    isAodActive = isAodActive
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 控制按钮
            AnimatedVisibility(
                visible = !isAodActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = {
                            onToggleClick()
                        },
                        modifier = Modifier.size(72.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (state.isBreakMode) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (state.isBreakMode) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        AnimatedContent(
                            targetState = state.isRunning,
                            transitionSpec = {
                                scaleIn(tween(200)) + fadeIn() togetherWith scaleOut(tween(200)) + fadeOut()
                            }, label = "play_pause_anim"
                        ) { isRunning ->
                            Icon(
                                imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (isRunning) "暂停" else "开始",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    FilledIconButton(
                        onClick = {
                            onResetClick()
                        },
                        modifier = Modifier.size(72.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "重置",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimerScreenPreview() {
    PomodoroTheme {
        TimerScreenContent(
            state = TimerState(timeRemaining = 25 * 60, isRunning = false),
            isAodActive = false,
            onAodToggle = {},
            onToggleClick = {},
            onResetClick = {},
            onOpenSettings = {},
            onTimeTextClick = {}
        )
    }
}
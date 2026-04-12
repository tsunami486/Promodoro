package com.example.promodoro.ui.screens

import android.app.Activity
import android.service.autofill.OnClickAction
import android.view.WindowInsetsController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.promodoro.model.TimerState
import com.example.promodoro.utils.TimeUtils
import com.example.promodoro.viewmodel.TimerViewModel
import com.example.promodoro.ui.theme.PomodoroTheme
import com.example.promodoro.utils.AlarmUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = viewModel(),
    onNavigateToSettings:()-> Unit
) {
    val state by viewModel.uiState.collectAsState()
    // 控制底部抽屉
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    //获取生命周期
    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current

    LaunchedEffect(state.alarmTrigger) {
        if (state.alarmTrigger > 0){
            AlarmUtils.playAlarmAndVibrate(context)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver {_,event ->
            if (event == Lifecycle.Event.ON_PAUSE){
                viewModel.handleAppBackground()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    if (state.isFocusFailed && state.isImmersiveModeEnabled){
        AlertDialog(
            onDismissRequest = {},
            title = {Text("专注失败")},
            text = {Text("由于你离开了，专注已中断。下次请保持专注哦！")},
            confirmButton = {
                Button(onClick = {viewModel.clearFocusFailure()}) {
                    Text("我知道了")
                }
            }
        )
    }

    //全屏控制
    val view = LocalView.current
    if(!view.isInEditMode){
        val window = (view.context as Activity).window
        val insetsController = remember { WindowCompat.getInsetsController(window,view) }
        LaunchedEffect(state.isRunning, state.isImmersiveModeEnabled) {
            if (state.isRunning && state.isImmersiveModeEnabled) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // 将状态和事件传递给纯 UI 组件
    TimerScreenContent(
        modifier = modifier,
        state = state,
        onToggleClick = { viewModel.toggleTimer() },
        onResetClick = { viewModel.resetTimer() },
        onOpenSettings = onNavigateToSettings,
        onTimeTextClick = {if(!state.isRunning) showSheet = true}
    )

    // 抽屉
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            SettingsSheetContent(
                currentBreakMinutes = state.breakTimeLength / 60 ,
                currentFocusMinutes = state.focusTimeLength / 60,
                onSave = { newFocusMinutes, newBreakMinutes ->
                    // 调用 ViewModel 的新方法
                    viewModel.updateTimeSettings(newFocusMinutes, newBreakMinutes)
                    // 关闭抽屉
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
    onToggleClick: () -> Unit,
    onResetClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onTimeTextClick: ()-> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 设置按钮
        if (!state.isRunning) {
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置"
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isRunning) {
                Text(
                    text = if (state.isBreakMode) "休息时间，休息一下吧" else "专注中",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (state.isBreakMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !state.isRunning) {onTimeTextClick()}
                    .padding(16.dp)
            ){
                // 显示倒计时时间
                Text(
                    text = TimeUtils.formatTime(state.timeRemaining),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (state.isBreakMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary

                )
            }


            Spacer(modifier = Modifier.height(32.dp))

            // 控制按钮行
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onToggleClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(state.isBreakMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (state.isRunning) "暂停" else "开始")
                }

                Button(
                    onClick = onResetClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ) {
                    Text("重置")
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
            onToggleClick = {},
            onResetClick = {},
            onOpenSettings = {},
            onTimeTextClick = {}
        )
    }
}
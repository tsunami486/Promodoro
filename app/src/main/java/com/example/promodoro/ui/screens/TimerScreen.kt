package com.example.promodoro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.promodoro.model.TimerState
import com.example.promodoro.utils.TimeUtils
import com.example.promodoro.viewmodel.TimerViewModel
import com.example.promodoro.ui.theme.PomodoroTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = viewModel()
) {
    // 收集 ViewModel 中的状态
    val state by viewModel.uiState.collectAsState()

    // 控制底部抽屉
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    // 将状态和事件传递给纯 UI 组件
    TimerScreenContent(
        modifier = modifier,
        state = state,
        onToggleClick = { viewModel.toggleTimer() },
        onResetClick = { viewModel.resetTimer() },
        onOpenSettings = { showSheet = true }
    )

    // 抽屉
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            SettingsSheetContent(
                currentMinutes = state.timeRemaining / 60,
                onSave = { newMinutes ->
                    viewModel.resetTimer(newMinutes)
                    viewModel.setWorkTime(newMinutes)
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
    onOpenSettings: () -> Unit
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
            // 显示倒计时时间
            Text(
                text = TimeUtils.formatTime(state.timeRemaining),
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 控制按钮行
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onToggleClick) {
                    Text(if (state.isRunning) "暂停" else "开始")
                }

                Button(onClick = onResetClick) {
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
            onOpenSettings = {}
        )
    }
}
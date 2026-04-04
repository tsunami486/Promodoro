package com.example.promodoro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.promodoro.utils.TimeUtils
import com.example.promodoro.viewmodel.TimerViewModel

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
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
            Button(onClick = { viewModel.toggleTimer() }) {
                Text(if (state.isRunning) "暂停" else "开始")
            }

            Button(onClick = { viewModel.resetTimer() }) {
                Text("重置")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview(){
    TimerScreen()
}
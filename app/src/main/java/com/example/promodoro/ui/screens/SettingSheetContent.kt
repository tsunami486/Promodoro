package com.example.promodoro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.promodoro.ui.components.VerticalNumberPicker
import com.example.promodoro.ui.theme.PomodoroTheme

// 抽屉内部设置UI
@Composable
fun SettingsSheetContent(
    currentMinutes: Int,
    onSave: (Int) -> Unit
) {
    // 内部记录分钟数
    var selectMinute by remember { mutableIntStateOf(if (currentMinutes > 0) currentMinutes else 25) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("选择专注时长", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.padding(24.dp))

        // 竖直滚轮
        Row(verticalAlignment = Alignment.CenterVertically) {
            VerticalNumberPicker(
                range = 1..60,
                currentValue = selectMinute,
                onValueChange = { selectMinute = it },
                modifier = Modifier.width(100.dp)
            )
            Text(
                "分钟",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSave(selectMinute) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("确定")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsSheetPreview() {
    PomodoroTheme {
        SettingsSheetContent(
            currentMinutes = 25,
            onSave = {}
        )
    }
}
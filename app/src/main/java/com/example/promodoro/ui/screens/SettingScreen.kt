package com.example.promodoro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.promodoro.viewmodel.TimerViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: TimerViewModel,
    onNavigateBack: ()-> Unit
){
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("设置")},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 16.dp)
        ) {
            //专注模式
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("专注模式", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "离开界面后自动暂停计时",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.isFocusModeEnabled,
                    onCheckedChange = { viewModel.setFocusMode(it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            //严格模式
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("严格模式", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "专注时切出会导致计时重置",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = state.isImmersiveModeEnabled,
                    onCheckedChange = {viewModel.setImmersiveMode(it)}
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 动态取色
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("动态取色", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "使应用配色跟随手机壁纸",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.isDynamicColorEnabled,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("AOD模式", style = MaterialTheme.typography.titleMedium)
                }
                Switch(
                    checked = state.isAodModeEnabled,
                    onCheckedChange = { viewModel.setAodMode(it) }
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

    }
}

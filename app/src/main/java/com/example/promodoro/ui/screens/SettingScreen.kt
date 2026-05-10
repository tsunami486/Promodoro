package com.example.promodoro.ui.screens

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.promodoro.viewmodel.TimerViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    innerPadding: PaddingValues,
    viewModel: TimerViewModel,
    onNavigateBack: ()-> Unit
){
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val notificationManager = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    var showDndDialog by remember { mutableStateOf(false) } // 新增：勿扰权限弹窗状态



    if (showDndDialog) {
        AlertDialog(
            onDismissRequest = { showDndDialog = false },
            title = { Text("需要“勿扰模式”权限") },
            text = {
                Text("为了在专注期间屏蔽微信、短信等外部打扰，应用需要控制系统的勿扰模式。\n\n请在即将打开的页面中，找到“番茄钟”并允许模式访问权限。")
            },
            confirmButton = {
                Button(onClick = {
                    showDndDialog = false
                    try {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }) {
                    Text("去授权")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDndDialog = false }) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("设置",style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            // 检查是否有勿扰权限
                            if (!notificationManager.isNotificationPolicyAccessGranted) {
                                showDndDialog = true
                            } else {
                                viewModel.setFocusMode(true)
                            }
                        } else {
                            viewModel.setFocusMode(false)
                        }
                    }
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = state.isImmersiveModeEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            // 检查是否有勿扰权限
                            if (!notificationManager.isNotificationPolicyAccessGranted) {
                                showDndDialog = true
                            } else {
                                viewModel.setImmersiveMode(true)
                            }
                        } else {
                            viewModel.setImmersiveMode(false)
                        }
                    }
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
                    Text("AOD", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "开启后在计时时点击屏幕可以切换AOD模式",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

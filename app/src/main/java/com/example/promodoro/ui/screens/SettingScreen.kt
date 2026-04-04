package com.example.promodoro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.promodoro.viewmodel.TimerViewModel




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: TimerViewModel,
    onNavigateBack: ()-> Unit
){
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //后面准备把按钮换成输入框或者竖向滚轮
            Text("选择专注时长", style = MaterialTheme.typography.titleLarge)

            //这是初步的，直接设置时间导致重置无法与设置的分钟数同步
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = {viewModel.setWorkTime(15);onNavigateBack()}) {
                    Text("15分钟")
                }
                Button(onClick = {viewModel.setWorkTime(25);onNavigateBack()}) {
                    Text("25分钟")
                }
                Button(onClick = {viewModel.setWorkTime(45);onNavigateBack()}) {
                    Text("45分钟")
                }
            }
        }
    }
}
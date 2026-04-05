package com.example.promodoro.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.promodoro.model.TimerState
import com.example.promodoro.utils.TimeUtils
import com.example.promodoro.viewmodel.TimerViewModel
import com.example.promodoro.ui.theme.PomodoroTheme
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = viewModel()
) {
    // 收集 ViewModel 中的状态
    val state by viewModel.uiState.collectAsState()

    //控制底部抽屉
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    // 将状态和事件传递给纯 UI 组件
    TimerScreenContent(
        modifier = modifier,
        state = state,
        onToggleClick = { viewModel.toggleTimer() },
        onResetClick = { viewModel.resetTimer() },
        onOpenSettings = {showSheet = true}
    )

    //抽屉
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {showSheet = false},
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle()}
        ) {
            SettingsSheetContent(
                currentMinutes = state.timeRemaining / 60,
                onSave = {newMinutes->
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
    onOpenSettings:() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        //设置按钮
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

//抽屉内部设置
@Composable
fun SettingsSheetContent(
    currentMinutes: Int,
    onSave:(Int)-> Unit
){
    //内部记录分钟数
    var selectMinute by remember { mutableIntStateOf(if(currentMinutes>0) currentMinutes else 25) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("选择专注时长", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.padding(24.dp))
        //竖直滚轮
        Row(verticalAlignment = Alignment.CenterVertically) {
            VerticalNumberPicker(
                range = 1..60,
                currentValue = selectMinute,
                onValueChange = {selectMinute = it},
                modifier = Modifier.width(100.dp)
            )
            Text("分钟", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {onSave(selectMinute)},
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("确定")
        }
        Spacer(modifier = Modifier.height(16.dp))

    }
}

//竖直滚轮
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerticalNumberPicker(
    range: IntRange,
    currentValue: Int,
    onValueChange:(Int)-> Unit,
    modifier: Modifier = Modifier
){
    val itemHeight = 48.dp
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (currentValue-range.first).coerceAtLeast(0)
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@derivedStateOf 0
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val centerItem = visibleItemsInfo.minByOrNull {
                kotlin.math.abs(it.offset + (it.size / 2) - viewportCenter)
            }
            centerItem?.index ?: 0
        }
    }

    LaunchedEffect(centerIndex) {
        val selectValue = range.elementAtOrNull(centerIndex)
        if (selectValue != null && selectValue != currentValue ){
            onValueChange(selectValue)
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.height(itemHeight * 3),
        contentPadding = PaddingValues(vertical = itemHeight)
    ) {
        items(range.count()) { index ->
            val value = range.elementAt(index)
            val isSelected = index == centerIndex
            Box(modifier = Modifier.height(itemHeight).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = value.toString(),
                    style = if (isSelected) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
                )
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

@Preview(showBackground = true)
@Composable
fun SettingsSheetPreview() {
    PomodoroTheme {
        // 直接预览抽屉内部的 UI，不需要点击，一目了然
        SettingsSheetContent(
            currentMinutes = 25,
            onSave = {} // 预览时不需要真正的保存逻辑
        )
    }
}
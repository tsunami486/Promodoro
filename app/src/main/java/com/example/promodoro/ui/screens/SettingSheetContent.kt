package com.example.promodoro.ui.screens

import android.R
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.TabRow
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.promodoro.ui.components.VerticalNumberPicker
import com.example.promodoro.ui.theme.PomodoroTheme
import kotlinx.coroutines.launch

// 抽屉内部设置UI
@Composable
fun SettingsSheetContent(
    currentFocusMinutes: Int,
    currentBreakMinutes: Int,
    onSave: (Int, Int) -> Unit
) {
    // 分别记录专注和休息的选中值
    var selectFocusMinute by remember { mutableIntStateOf(if (currentFocusMinutes > 0) currentFocusMinutes else 25) }
    var selectBreakMinute by remember { mutableIntStateOf(if (currentBreakMinutes > 0) currentBreakMinutes else 5) }
    val pagerState = rememberPagerState(pageCount = {2})
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ================= 顶部指示器 =================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp) // 指示器的高度，可以根据喜好改成 6.dp
        ) {
            // 左侧（专注）颜色动画：选中时亮起主色调，未选中时变为透明（背景色）
            val leftColor by animateColorAsState(
                targetValue = if (pagerState.currentPage == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "leftColor"
            )
            // 右侧（休息）颜色动画：选中时亮起次色调(绿色)，未选中时变为透明
            val rightColor by animateColorAsState(
                targetValue = if (pagerState.currentPage == 1) MaterialTheme.colorScheme.secondary else Color.Transparent,
                label = "rightColor"
            )

            // 左半部分
            Box(
                modifier = Modifier
                    .weight(1f) // weight(1f) 保证左右严格平分宽度
                    .fillMaxHeight()
                    .background(leftColor)
                    // 加上点击事件：点左半边也能直接切换过去
                    .clickable { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
            )
            // 右半部分
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(rightColor)
                    .clickable { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { page ->


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (page == 0) {
                    Text("调整专注时间", style = MaterialTheme.typography.titleLarge)
                }else{
                    Text("调整休息时间", style = MaterialTheme.typography.titleLarge)
                }

                // 根据当前页码显示不同的内容
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (page == 0) {
                        // 第一页：专注时间滚轮
                        VerticalNumberPicker(
                            range = 1..60,
                            currentValue = selectFocusMinute,
                            onValueChange = { selectFocusMinute = it },
                            modifier = Modifier.width(80.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        // 第二页：休息时间滚轮
                        VerticalNumberPicker(
                            range = 1..30,
                            currentValue = selectBreakMinute,
                            onValueChange = { selectBreakMinute = it },
                            modifier = Modifier.width(80.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Text(
                        "分钟",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { onSave(selectFocusMinute,selectBreakMinute) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 20.dp)
        ) {
            Text("确定")
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsSheetPreview() {
    PomodoroTheme {
        SettingsSheetContent(
            currentFocusMinutes = 25,
            currentBreakMinutes = 5,
            onSave = {workMinutes,breakMinutes -> }
        )
    }
}
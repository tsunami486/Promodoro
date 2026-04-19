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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp) // 指示器的高度，可以根据喜好改成 6.dp
        ) {
            val leftColor by animateColorAsState(
                targetValue = if (pagerState.currentPage == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "leftColor"
            )
            val rightColor by animateColorAsState(
                targetValue = if (pagerState.currentPage == 1) MaterialTheme.colorScheme.secondary else Color.Transparent,
                label = "rightColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(leftColor)
                    .clickable { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
            )
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

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (page == 0) {
                        VerticalNumberPicker(
                            range = 1..60,
                            currentValue = selectFocusMinute,
                            onValueChange = { selectFocusMinute = it },
                            modifier = Modifier.width(80.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
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
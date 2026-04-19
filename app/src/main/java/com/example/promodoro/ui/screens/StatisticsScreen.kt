package com.example.promodoro.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.promodoro.ui.theme.PomodoroTheme

data class StatisticsState(
    val todayFocusMinutes: Int = 0,
    val todayBreakMinutes: Int = 0,
    val weekTotalFocusMinutes: Int = 0,
    val weekDays: List<String> = listOf("一", "二", "三", "四", "五", "六", "日"),
    val focusTimes: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
)

fun formatHoursMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours} 小时 ${minutes} 分" else "${minutes} 分钟"
}

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    state: StatisticsState
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("统计", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Text("今日", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        // 3. 顶部概览卡片使用 state 中的数据
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                title = "专注",
                value = formatHoursMinutes(state.todayFocusMinutes),
                modifier = Modifier.weight(1f),
                isPrimary = true
            )
            SummaryCard(
                title = "休息",
                value = formatHoursMinutes(state.todayBreakMinutes),
                modifier = Modifier.weight(1f),
                isPrimary = false
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 图表卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("本周专注", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "共 ${formatHoursMinutes(state.weekTotalFocusMinutes)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                InteractiveBarChart(
                    focusTimes = state.focusTimes,
                    weekDays = state.weekDays
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier, isPrimary: Boolean) {
    val containerColor = if (isPrimary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = contentColor, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            Text(value, color = contentColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun InteractiveBarChart(focusTimes: List<Float>, weekDays: List<String>) {
    val maxTime = (focusTimes.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val yLabels = listOf(
        maxTime.toInt().toString(),
        (maxTime * 2 / 3).toInt().toString(),
        (maxTime / 3).toInt().toString(),
        "0"
    )

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { animationPlayed = true }

    val chartHeight = 220.dp
    val topPadding = 32.dp
    val bottomPadding = 28.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        // Y轴区域
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = topPadding, bottom = bottomPadding)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            yLabels.forEach { label ->
                if (label != "0"){
                Text(
                    text = label+"分钟",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )}
            }
            Text(text = "0分钟", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }

        // 柱状图区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            val barWidth = size.width / focusTimes.size
                            selectedIndex = (offset.x / barWidth).toInt().coerceIn(0, focusTimes.lastIndex)
                        },
                        onDrag = { change, _ ->
                            val barWidth = size.width / focusTimes.size
                            selectedIndex = (change.position.x / barWidth).toInt().coerceIn(0, focusTimes.lastIndex)
                        },
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null }
                    )
                }
        ) {
//            // 背景参考线
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(top = topPadding, bottom = bottomPadding),
//                verticalArrangement = Arrangement.SpaceBetween
//            ) {
//                repeat(4) {
//                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
//                }
//            }

            // 柱子
            Row(modifier = Modifier.fillMaxSize()) {
                focusTimes.forEachIndexed { index, time ->
                    val isSelected = selectedIndex == index
                    val fraction = time / maxTime

                    val animatedFraction by animateFloatAsState(
                        targetValue = if (animationPlayed) fraction else 0f,
                        animationSpec = tween(durationMillis = 800, delayMillis = index * 50),
                        label = "bar_anim"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (isSelected) {
                                Surface(
                                    modifier = Modifier.align(Alignment.TopCenter),
                                    color = MaterialTheme.colorScheme.inverseSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Text(
                                        text = "${time.toInt()}分",
                                        color = MaterialTheme.colorScheme.inverseOnSurface,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .padding(top = topPadding)
                                    .width(28.dp)
                                    .fillMaxHeight(animatedFraction.coerceAtLeast(0.02f))
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = weekDays[index],
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticScreenPreview() {
    PomodoroTheme {
        StatisticsScreen(
            state = StatisticsState(
                todayFocusMinutes = 135,
                todayBreakMinutes = 25,
                weekTotalFocusMinutes = 330,
                focusTimes = listOf(10f, 45f, 30f, 60f, 20f, 120f, 90f)
            )
        )
    }
}
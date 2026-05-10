package com.example.promodoro.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.promodoro.ui.theme.PomodoroTheme

data class DailyStatisticsDetailState(
    val date: String,
    val totalFocusMinutes: Int = 0,
    val periods: List<PeriodFocusStat> = emptyList()
)

data class PeriodFocusStat(
    val label: String,
    val rangeLabel: String,
    val minutes: Int,
    val ratio: Float
)

private val periodColors = listOf(
    Color(0xFF7E57C2),
    Color(0xFFFFCA28),
    Color(0xFF26A69A),
    Color(0xFFFF7043)
)

@Composable
fun StatisticsDetailScreen(
    innerPadding: PaddingValues,
    state: DailyStatisticsDetailState,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = "专注详情",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = state.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FocusPeriodDonutChart(state = state)

                Spacer(modifier = Modifier.height(24.dp))

                if (state.totalFocusMinutes == 0) {
                    Text(
                        text = "这一天还没有专注记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    PeriodList(periods = state.periods)
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun FocusPeriodDonutChart(state: DailyStatisticsDetailState) {
    val chartStrokeWidth = 38.dp
    val fallbackColor = MaterialTheme.colorScheme.outlineVariant

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (state.totalFocusMinutes == 0) {
                drawArc(
                    color = fallbackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = chartStrokeWidth.toPx(), cap = StrokeCap.Round)
                )
            } else {
                var startAngle = -90f
                state.periods.forEachIndexed { index, period ->
                    val sweepAngle = period.ratio * 360f
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = periodColors[index % periodColors.size],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = chartStrokeWidth.toPx(), cap = StrokeCap.Butt)
                        )
                    }
                    startAngle += sweepAngle
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "总计",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatHoursMinutes(state.totalFocusMinutes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PeriodList(periods: List<PeriodFocusStat>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        periods.forEachIndexed { index, period ->
            PeriodRow(
                stat = period,
                color = periodColors[index % periodColors.size]
            )
        }
    }
}

@Composable
private fun PeriodRow(stat: PeriodFocusStat, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stat.label} ${stat.rangeLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(stat.ratio * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(stat.ratio.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(color)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatHoursMinutes(stat.minutes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsDetailScreenPreview() {
    PomodoroTheme {
        StatisticsDetailScreen(
            innerPadding = PaddingValues(10.dp),
            state = DailyStatisticsDetailState(
                date = "2026-05-10",
                totalFocusMinutes = 180,
                periods = listOf(
                    PeriodFocusStat("凌晨", "00:00-06:00", 10, 10f / 180f),
                    PeriodFocusStat("上午", "06:00-12:00", 60, 60f / 180f),
                    PeriodFocusStat("下午", "12:00-18:00", 80, 80f / 180f),
                    PeriodFocusStat("晚上", "18:00-24:00", 30, 30f / 180f)
                )
            ),
            onNavigateBack = {}
        )
    }
}

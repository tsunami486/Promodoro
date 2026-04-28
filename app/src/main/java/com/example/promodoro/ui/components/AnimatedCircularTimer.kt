package com.example.promodoro.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.promodoro.utils.TimeUtils

@Composable
fun AnimatedCircularTimer(
    timeRemaining: Int,
    totalTime: Int,
    isRunning: Boolean,
    isBreakMode: Boolean,
    isAodActive: Boolean,
    modifier: Modifier = Modifier
) {
    // 采用 Animatable 进行精准的动画两步控制
    val visualProgress = remember { Animatable(0f) }

    LaunchedEffect(timeRemaining, isRunning, totalTime) {
        // 当计时停止且时间为满时，进度瞬间归零（只显示底层灰色轨道）
        if (!isRunning && timeRemaining == totalTime) {
            visualProgress.animateTo(0f,tween(1000, easing = FastOutSlowInEasing) )
            return@LaunchedEffect
        }

        if (isRunning) {
            val targetProgress = if (totalTime > 0) timeRemaining.toFloat() / totalTime else 1f

            if (visualProgress.value == 0f) {
                visualProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                )
            }

            visualProgress.animateTo(
                targetValue = targetProgress,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
        }

    }

    val progressColor by animateColorAsState(
        targetValue = if (isBreakMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 800),
        label = "color_anim"
    )

    val textColor by animateColorAsState(
        targetValue = if (isAodActive) Color.DarkGray else MaterialTheme.colorScheme.onSurface,
        label = "text_color"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(300.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()

            drawCircle(
                color = progressColor.copy(alpha = if (isAodActive) 0.2f else 0.1f),
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                color = if(isAodActive) Color.DarkGray else progressColor,
                startAngle = -90f,
                sweepAngle = -360f * visualProgress.value,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        RollingTimeText(
            timeString = TimeUtils.formatTime(timeRemaining),
            textColor = textColor
        )
    }
}

@Composable
fun RollingTimeText(
    timeString: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    // 使用 Row 将拆散的数字横向排列
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 遍历时间字符串（例如 "24:59"）的每一个字符
        timeString.forEachIndexed { index, char ->
            // 如果是冒号 ":"，不需要任何动画，直接静态渲染
            if (char == ':') {
                Text(
                    text = ":",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 80.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = textColor,
                    modifier = Modifier.padding(bottom = 6.dp) // 稍微向上微调冒号对齐
                )
            } else {
                // 如果是数字，套上 AnimatedContent
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        // 【核心动画引擎】
                        // 倒计时是数字变小，视觉上新数字从上面掉下来，旧数字从下面滑走
                        // slideInVertically { -it } 代表从自身高度的上方进入
                        // slideOutVertically { it } 代表滑向自身高度的下方
                        (slideInVertically { -it } + fadeIn()) togetherWith
                                (slideOutVertically { it } + fadeOut())
                    },
                    label = "digit_anim_$index"
                ) { currentDigit ->
                    Text(
                        text = currentDigit.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 80.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = textColor
                    )
                }
            }
        }
    }
}
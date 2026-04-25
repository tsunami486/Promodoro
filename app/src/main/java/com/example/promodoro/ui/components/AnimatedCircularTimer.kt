import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.promodoro.utils.TimeUtils // 确保导入你的时间格式化工具

@Composable
fun AnimatedCircularTimer(
    timeRemaining: Int,
    totalTime: Int,
    isBreakMode: Boolean,
    modifier: Modifier = Modifier
) {
    // 1. 计算当前进度的百分比 (0.0 到 1.0)
    val progress = if (totalTime > 0) timeRemaining.toFloat() / totalTime else 1f

    // 2. 进度条平滑动画：即使时间是一秒一秒跳的，进度条却是像流水一样平滑滑动的！
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        // 使用 LinearEasing 让每秒的滑动看起来连贯不断
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "progress_anim"
    )

    // 3. 颜色平滑切换动画：专注是主题色，休息是清新的绿色
    val progressColor by animateColorAsState(
        targetValue = if (isBreakMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 800),
        label = "color_anim"
    )

    // 4. UI 布局：Box 让 Canvas(圆环) 和 Text(时间) 完美重叠居中
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(320.dp) // 给圆环一个足够震撼的大尺寸
    ) {
        // 画布层
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx() // 圆环的粗细

            // 底层圆环：作为轨道，半透明显示
            drawCircle(
                color = progressColor.copy(alpha = 0.15f),
                style = Stroke(width = strokeWidth)
            )

            // 表层圆弧：真正的倒计时进度条
            drawArc(
                color = progressColor,
                startAngle = -90f, // -90度代表从钟表的 12 点钟方向开始画
                sweepAngle = -360f * animatedProgress, // 根据进度计算扫过的角度
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // 中心数字层
        Text(
            text = TimeUtils.formatTime(timeRemaining),
            // 使用巨型字体，并加上一点字体粗细，提升冲击力
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 76.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
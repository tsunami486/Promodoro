package com.example.promodoro.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.promodoro.ui.screens.InteractiveBarChart

// 定义导航项的数据类
data class NavItem(val name: String, val route: String, val unSelectIcon: ImageVector,val selectIcon : ImageVector)

@Composable
fun FloatingBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem("计时", "timer", Icons.Outlined.Timer,Icons.Default.Timer),
        NavItem("统计", "statistics", Icons.Outlined.BarChart,Icons.Default.BarChart),
        NavItem("设置", "settings", Icons.Outlined.Settings,Icons.Default.Settings)
    )

    // 悬浮容器
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        // 药丸形状的背景
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 8.dp,
            modifier = Modifier.height(64.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val interactionSource = remember { MutableInteractionSource() }
                    // 选中状态的背景色和图标颜色动画
                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        label = "nav_bg_color"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "nav_content_color"
                    )

                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(backgroundColor)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null) { onNavigate(item.route) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        // 选中时显示文字，未选中只显示图标
                        if (isSelected) {
                            Icon(
                                imageVector = item.selectIcon,
                                contentDescription = item.name,
                                tint = contentColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.name,
                                color = contentColor,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }else{
                            Icon(
                                imageVector = item.unSelectIcon,
                                contentDescription = item.name,
                                tint = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}
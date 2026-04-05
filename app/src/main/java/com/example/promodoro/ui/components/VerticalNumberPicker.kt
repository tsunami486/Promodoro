package com.example.promodoro.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// 竖直滚轮通用组件
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerticalNumberPicker(
    range: IntRange,
    currentValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 48.dp
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (currentValue - range.first).coerceAtLeast(0)
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
        if (selectValue != null && selectValue != currentValue) {
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
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.toString(),
                    style = if (isSelected) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
                )
            }
        }
    }
}
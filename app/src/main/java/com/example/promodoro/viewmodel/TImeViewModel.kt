package com.example.promodoro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.promodoro.data.FocusRecord
import com.example.promodoro.data.FocusRepository
import com.example.promodoro.model.TimerState
import com.example.promodoro.ui.screens.DailyStatisticsDetailState
import com.example.promodoro.ui.screens.PeriodFocusStat
import com.example.promodoro.ui.screens.StatisticsState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val MINUTES_PER_DAY = 24 * 60
private const val MILLIS_PER_MINUTE = 60_000L

private data class FocusPeriodDefinition(
    val label: String,
    val rangeLabel: String,
    val startMinute: Int,
    val endMinute: Int
)

private val focusPeriodDefinitions = listOf(
    FocusPeriodDefinition("凌晨", "00:00-06:00", 0, 6 * 60),
    FocusPeriodDefinition("上午", "06:00-12:00", 6 * 60, 12 * 60),
    FocusPeriodDefinition("下午", "12:00-18:00", 12 * 60, 18 * 60),
    FocusPeriodDefinition("晚上", "18:00-24:00", 18 * 60, MINUTES_PER_DAY)
)

fun emptyDailyStatisticsDetailState(date: String): DailyStatisticsDetailState {
    return DailyStatisticsDetailState(
        date = date,
        totalFocusMinutes = 0,
        periods = focusPeriodDefinitions.map {
            PeriodFocusStat(
                label = it.label,
                rangeLabel = it.rangeLabel,
                minutes = 0,
                ratio = 0f
            )
        }
    )
}

fun calculateDailyStatisticsDetail(date: String, records: List<FocusRecord>): DailyStatisticsDetailState {
    val dateStartMillis = parseDateStartMillis(date) ?: return emptyDailyStatisticsDetailState(date)
    val periodMinutes = MutableList(focusPeriodDefinitions.size) { 0 }

    records.forEach { record ->
        if (record.focusMinutes <= 0) return@forEach

        val recordStartMinute = Math.floorDiv(record.timestamp - dateStartMillis, MILLIS_PER_MINUTE)
        val recordEndMinute = recordStartMinute + record.focusMinutes

        focusPeriodDefinitions.forEachIndexed { index, period ->
            val overlapStart = maxOf(recordStartMinute, period.startMinute.toLong())
            val overlapEnd = minOf(recordEndMinute, period.endMinute.toLong())
            val overlapMinutes = (overlapEnd - overlapStart).coerceAtLeast(0L).toInt()
            periodMinutes[index] += overlapMinutes
        }
    }

    val totalFocusMinutes = periodMinutes.sum()
    val periods = focusPeriodDefinitions.mapIndexed { index, period ->
        val minutes = periodMinutes[index]
        PeriodFocusStat(
            label = period.label,
            rangeLabel = period.rangeLabel,
            minutes = minutes,
            ratio = if (totalFocusMinutes > 0) minutes.toFloat() / totalFocusMinutes else 0f
        )
    }

    return DailyStatisticsDetailState(
        date = date,
        totalFocusMinutes = totalFocusMinutes,
        periods = periods
    )
}

private fun parseDateStartMillis(date: String): Long? {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        isLenient = false
    }
    return runCatching { formatter.parse(date)?.time }.getOrNull()
}

class TimerViewModel(private val repository: FocusRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerState())
    val uiState: StateFlow<TimerState> = _uiState.asStateFlow()
    private var timerJob: Job? = null
    private var currentRecordId: Long? = null
    private val dailyStatisticsDetailStates = mutableMapOf<String, StateFlow<DailyStatisticsDetailState>>()

    val statisticsState: StateFlow<StatisticsState> = repository.allRecords.map { records ->
        calculateStatistics(records)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StatisticsState()
    )

    fun dailyStatisticsDetailState(date: String): StateFlow<DailyStatisticsDetailState> {
        return dailyStatisticsDetailStates.getOrPut(date) {
            repository.allRecords.map { records ->
                calculateDailyStatisticsDetail(date, records)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyDailyStatisticsDetailState(date)
            )
        }
    }

    // 切换计时器的播放/暂停状态
    fun toggleTimer() {
        if (_uiState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = true) }

        timerJob = viewModelScope.launch {
            val state = _uiState.value
            if (!state.isBreakMode && currentRecordId == null) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Date())
                currentRecordId = repository.insertRecord(
                    FocusRecord(date = todayStr, focusMinutes = 0, breakMinutes = 0)
                )
            }

            while (_uiState.value.isRunning && _uiState.value.timeRemaining > 0) {
                delay(1000L)
                _uiState.update { it.copy(timeRemaining = it.timeRemaining - 1) }

                val currentState = _uiState.value
                if (!currentState.isBreakMode) {
                    val elapsedSeconds = currentState.focusTimeLength - currentState.timeRemaining

                    if (elapsedSeconds > 0 && elapsedSeconds % 60 == 0) {
                        val elapsedMinutes = elapsedSeconds / 60
                        currentRecordId?.let { id ->
                            repository.updateFocusMinutes(id, elapsedMinutes)
                        }
                    }
                }
            }

            // 倒计时归零的切换逻辑
            if (_uiState.value.timeRemaining == 0) {
                val currentState = _uiState.value
                if (!currentState.isBreakMode) {
                    currentRecordId = null
                    _uiState.update {
                        it.copy(
                            isBreakMode = true,
                            timeRemaining = it.breakTimeLength,
                            alarmTrigger = it.alarmTrigger + 1,
                            isRunning = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isBreakMode = false,
                            timeRemaining = it.focusTimeLength,
                            isRunning = false,
                            alarmTrigger = it.alarmTrigger + 1
                        )
                    }
                }
            }
        }
    }


    private fun pauseTimer() {
        _uiState.update { it.copy(isRunning = false) }
        timerJob?.cancel()
    }
    fun resetTimer() {
        pauseTimer()
        currentRecordId = null
        _uiState.update { it.copy(timeRemaining = it.focusTimeLength) }
        _uiState.update { it.copy(isBreakMode = false) }
    }

    fun updateTimeSettings(workMinutes: Int,breakMinutes: Int) {
        pauseTimer()
        currentRecordId = null
        _uiState.update {
            it.copy(
                timeRemaining = workMinutes * 60,
                focusTimeLength = workMinutes * 60,
                breakTimeLength = breakMinutes * 60,
                isBreakMode = false
            )
        }
    }

    fun setImmersiveMode(enabled: Boolean) {
        _uiState.update {
            it.copy(isImmersiveModeEnabled = enabled)
        }
    }

    fun setFocusMode(enabled: Boolean) {
        _uiState.update { it.copy(isFocusModeEnabled = enabled) }
    }

    fun handleAppBackground() {
        val state = _uiState.value
        if (state.isRunning && !state.isBreakMode) {
            when {
                state.isImmersiveModeEnabled -> {
                    pauseTimer()
                    _uiState.update { it.copy(isFocusFailed = true) }
                }
                state.isFocusModeEnabled -> {
                    pauseTimer()
                }
                else -> {
                    // Do nothing here
                }
            }
        }
    }

    fun clearFocusFailure() {
        _uiState.update { it.copy(isFocusFailed = false) }
        resetTimer()
    }

    fun setDynamicColor(enabled: Boolean){
        _uiState.update { it.copy(isDynamicColorEnabled = enabled) }
    }

    private fun saveFocusRecord(focusMins: Int, breakMins: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        viewModelScope.launch {
            val record = FocusRecord(
                date = todayStr,
                focusMinutes = focusMins,
                breakMinutes = breakMins
            )
            repository.insertRecord(record)
        }
    }

    private fun calculateStatistics(records: List<FocusRecord>): StatisticsState {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        val todayRecords = records.filter { it.date == todayStr }
        val todayFocus = calculateDailyStatisticsDetail(todayStr, records).totalFocusMinutes
        val todayBreak = todayRecords.sumOf { it.breakMinutes }

        val weekDays = mutableListOf<String>()
        val weekDates = mutableListOf<String>()
        val focusTimes = mutableListOf<Float>()

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(cal.time)

            val dayOfWeekFormat = SimpleDateFormat("E", Locale.CHINESE)
            val dayName = dayOfWeekFormat.format(cal.time).replace("周", "")

            val dailyFocus = calculateDailyStatisticsDetail(dateStr, records).totalFocusMinutes

            weekDays.add(if (i == 0) "今" else dayName)
            weekDates.add(dateStr)
            focusTimes.add(dailyFocus.toFloat())
        }

        val weekTotal = focusTimes.sum().toInt()

        val monthDays = mutableListOf<String>()
        val monthDates = mutableListOf<String>()
        val monthFocusTimes = mutableListOf<Float>()
        val monthCalendar = Calendar.getInstance()
        val todayDayOfMonth = monthCalendar.get(Calendar.DAY_OF_MONTH)

        for (day in 1..todayDayOfMonth) {
            monthCalendar.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = sdf.format(monthCalendar.time)
            val dailyFocus = calculateDailyStatisticsDetail(dateStr, records).totalFocusMinutes

            monthDays.add(day.toString())
            monthDates.add(dateStr)
            monthFocusTimes.add(dailyFocus.toFloat())
        }

        val monthTotal = monthFocusTimes.sum().toInt()

        return StatisticsState(
            todayFocusMinutes = todayFocus,
            todayBreakMinutes = todayBreak,
            weekTotalFocusMinutes = weekTotal,
            weekDays = weekDays,
            weekDates = weekDates,
            focusTimes = focusTimes,
            monthTotalFocusMinutes = monthTotal,
            monthDays = monthDays,
            monthDates = monthDates,
            monthFocusTimes = monthFocusTimes
        )
    }
    fun setAodMode(enabled: Boolean) {
        _uiState.update { it.copy(isAodModeEnabled = enabled) }
    }
}


class TimerViewModelFactory(private val repository: FocusRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

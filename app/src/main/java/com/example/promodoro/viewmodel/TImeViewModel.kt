package com.example.promodoro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.promodoro.data.FocusRecord
import com.example.promodoro.data.FocusRepository
import com.example.promodoro.model.TimerState
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
import kotlin.math.min

class TimerViewModel(private val repository: FocusRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerState())
    val uiState: StateFlow<TimerState> = _uiState.asStateFlow()
    private var timerJob: Job? = null
    private var currentRecordId: Long? = null
    val statisticsState: StateFlow<StatisticsState> = repository.allRecords.map { records ->
        calculateStatistics(records)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StatisticsState()
    )

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
            // ================= 核心修改 =================
            val state = _uiState.value
            // 如果是在专注模式下，且还没有记录 ID（说明是一次全新的开始，而不是暂停后继续）
            if (!state.isBreakMode && currentRecordId == null) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Date())
                // 先插入一条 0 分钟的空记录，并把返回的 ID 存起来
                currentRecordId = repository.insertRecord(
                    FocusRecord(date = todayStr, focusMinutes = 0, breakMinutes = 0)
                )
            }
            // ===========================================

            while (_uiState.value.isRunning && _uiState.value.timeRemaining > 0) {
                delay(1000L) // 过去1秒
                _uiState.update { it.copy(timeRemaining = it.timeRemaining - 1) }

                // ================= 每一分钟实时保存 =================
                val currentState = _uiState.value
                if (!currentState.isBreakMode) {
                    // 算出已经流逝了多少秒
                    val elapsedSeconds = currentState.focusTimeLength - currentState.timeRemaining

                    // 当流逝的秒数是 60 的倍数时（说明刚好走完一整分钟）
                    if (elapsedSeconds > 0 && elapsedSeconds % 60 == 0) {
                        val elapsedMinutes = elapsedSeconds / 60
                        // 实时更新数据库里的那一行的分钟数！
                        currentRecordId?.let { id ->
                            repository.updateFocusMinutes(id, elapsedMinutes)
                        }
                    }
                }
                // =================================================
            }

            // 倒计时归零的切换逻辑
            if (_uiState.value.timeRemaining == 0) {
                val currentState = _uiState.value
                if (!currentState.isBreakMode) {
                    // 专注结束 -> 休息
                    currentRecordId = null // 清空 ID，等待下一次专注
                    _uiState.update {
                        it.copy(
                            isBreakMode = true,
                            timeRemaining = it.breakTimeLength,
                            alarmTrigger = it.alarmTrigger + 1
                        )
                    }
                } else {
                    // 休息结束 -> 专注
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

    fun handleAppBackground() {
        if (_uiState.value.isRunning && !_uiState.value.isBreakMode) {
            pauseTimer()
            _uiState.update { it.copy(isFocusFailed = true) }
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
        val todayFocus = todayRecords.sumOf { it.focusMinutes }
        val todayBreak = todayRecords.sumOf { it.breakMinutes }

        val calendar = Calendar.getInstance()
        val weekDays = mutableListOf<String>()
        val focusTimes = mutableListOf<Float>()

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(cal.time)

            val dayOfWeekFormat = SimpleDateFormat("E", Locale.CHINESE)
            val dayName = dayOfWeekFormat.format(cal.time).replace("周", "") // 把"周一"变成"一"

            val dailyFocus = records.filter { it.date == dateStr }.sumOf { it.focusMinutes }

            weekDays.add(if (i == 0) "今" else dayName)
            focusTimes.add(dailyFocus.toFloat())
        }

        val weekTotal = focusTimes.sum().toInt()

        return StatisticsState(
            todayFocusMinutes = todayFocus,
            todayBreakMinutes = todayBreak,
            weekTotalFocusMinutes = weekTotal,
            weekDays = weekDays,
            focusTimes = focusTimes
        )
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

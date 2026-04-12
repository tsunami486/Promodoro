package com.example.promodoro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.promodoro.model.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min

class TimerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TimerState())
    val uiState: StateFlow<TimerState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

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
            while (_uiState.value.isRunning) {
                if(_uiState.value.timeRemaining > 0){
                    delay(1000L) // 等待1秒
                    _uiState.update { currentState ->
                        currentState.copy(timeRemaining = currentState.timeRemaining - 1)
                    }
                } else {
                    val currentState = _uiState.value
                    if(!currentState.isBreakMode){
                        _uiState.update {
                            it.copy(
                                isBreakMode = true,
                                timeRemaining = it.breakTimeLength,
                                alarmTrigger = it.alarmTrigger + 1
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isBreakMode = false,
                                timeRemaining = it.focusTimeLength,
                                alarmTrigger = it.alarmTrigger + 1
                            )
                        }
                    }
                }

            }
            if (_uiState.value.timeRemaining == 0) {
                _uiState.update { it.copy(isRunning = false) }
            }
        }
    }

    private fun pauseTimer() {
        _uiState.update { it.copy(isRunning = false) }
        timerJob?.cancel()
    }
    fun resetTimer() {
        pauseTimer()
        _uiState.update { it.copy(timeRemaining = it.focusTimeLength) }
        _uiState.update { it.copy(isBreakMode = false) }
    }

    fun updateTimeSettings(workMinutes: Int,breakMinutes: Int) {
        pauseTimer()
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
}
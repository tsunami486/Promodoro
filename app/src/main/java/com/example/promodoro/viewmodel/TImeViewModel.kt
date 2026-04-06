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
        _uiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemaining > 0 && _uiState.value.isRunning) {
                delay(1000L) // 等待1秒
                _uiState.update { currentState ->
                    currentState.copy(timeRemaining = currentState.timeRemaining - 1)
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

    fun resetTimer(minutes: Int) {
        pauseTimer()
        _uiState.update { it.copy(timeToRemaining = minutes * 60) }
        _uiState.update { it.copy(timeRemaining = it.timeToRemaining) }
    }

    fun resetTimer() {
        pauseTimer()
        _uiState.update { it.copy(timeRemaining = it.timeToRemaining) }
    }

    fun setWorkTime(minutes: Int) {
        pauseTimer()
        _uiState.update {
            it.copy(timeRemaining = minutes * 60)
        }
    }

    fun setImmersiveMode(enabled: Boolean) {
        _uiState.update {
            it.copy(isImmersiveModeEnabled = enabled)
        }
    }

    fun handleAppBackground() {
        if (_uiState.value.isRunning) {
            pauseTimer()
            _uiState.update { it.copy(isFocusFailed = true) }
        }
    }

    fun clearFocusFailure() {
        _uiState.update { it.copy(isFocusFailed = false) }
        resetTimer()
    }
}
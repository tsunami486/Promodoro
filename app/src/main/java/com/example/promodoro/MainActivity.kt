package com.example.promodoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.promodoro.ui.screens.SettingScreen
import com.example.promodoro.ui.screens.TimerScreen
import com.example.promodoro.ui.theme.PomodoroTheme
import com.example.promodoro.viewmodel.TimerViewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val timerViewModel: TimerViewModel = viewModel()
            val state by timerViewModel.uiState.collectAsState()

            PomodoroTheme(
                dynamicColor = state.isDynamicColorEnabled
            ) {
                Scaffold (
                    modifier = Modifier.fillMaxSize(),
                ) {innerPadding->
                    PomodoroApp(innerPadding,timerViewModel)
                }
            }
        }
    }
}

@Composable
fun PomodoroApp(innerPadding: PaddingValues,timerViewModel: TimerViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "timer"){
        composable("timer"){
            TimerScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = timerViewModel,
                onNavigateToSettings = {navController.navigate("settings")}
            )
        }

        composable("settings") {
            SettingScreen(
                viewModel = timerViewModel,
                onNavigateBack = {navController.popBackStack()}
            )
        }
    }
}
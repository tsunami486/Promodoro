package com.example.promodoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.promodoro.data.AppDatabase
import com.example.promodoro.data.FocusRepository
import com.example.promodoro.ui.components.FloatingBottomNav
import com.example.promodoro.ui.screens.StatisticsScreen
import com.example.promodoro.ui.screens.StatisticsState
import com.example.promodoro.viewmodel.TimerViewModelFactory

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { FocusRepository(database.focusDao()) }
    private val timerViewModel: TimerViewModel by viewModels {
        TimerViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val timerState by timerViewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        NavHost(
            navController = navController,
            startDestination = "timer",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(100)) },
            exitTransition = { fadeOut(animationSpec = tween(100)) }
        ){
            composable("timer"){
                TimerScreen(
                    viewModel = timerViewModel,
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable("statistics"){
                val statsState by timerViewModel.statisticsState.collectAsState()
                StatisticsScreen(state = statsState)
            }

            composable("settings") {
                SettingScreen(
                    viewModel = timerViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        AnimatedVisibility(
            visible = !timerState.isRunning,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight * 2 },
                animationSpec = tween(durationMillis = 400)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight * 2 },
                animationSpec = tween(durationMillis = 400)
            )
        ) {
            FloatingBottomNav(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
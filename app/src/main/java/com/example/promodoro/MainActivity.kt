package com.example.promodoro

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.promodoro.data.AppDatabase
import com.example.promodoro.data.FocusRepository
import com.example.promodoro.ui.components.FloatingBottomNav
import com.example.promodoro.ui.screens.SettingScreen
import com.example.promodoro.ui.screens.StatisticsDetailScreen
import com.example.promodoro.ui.screens.StatisticsScreen
import com.example.promodoro.ui.screens.TimerScreen
import com.example.promodoro.ui.theme.PomodoroTheme
import com.example.promodoro.viewmodel.TimerViewModel
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
        createNotificationChannel()
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
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "TimerChannel",
                "番茄钟倒计时",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "在后台显示剩余专注时间"
                enableLights(false)
                enableVibration(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

}
fun checkOverlayPermission(context: Context) {
    if (!Settings.canDrawOverlays(context)) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
    }
}
fun checkAndRequestOverlayPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!Settings.canDrawOverlays(context)) {
            Toast.makeText(context, "请开启悬浮窗权限", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
            return false
        }
    }
    return true
}

@Composable
fun PomodoroApp(innerPadding: PaddingValues, timerViewModel: TimerViewModel) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val timerState by timerViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var statisticsScrollAnimationKey by remember { mutableIntStateOf(0) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->

        }
    )

    checkOverlayPermission(context)
    checkAndRequestOverlayPermission(context)

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        NavHost(
            navController = navController,
            startDestination = "timer",
            modifier = Modifier.padding(),
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
                StatisticsScreen(
                    innerPadding = innerPadding,
                    state = statsState,
                    scrollAnimationKey = statisticsScrollAnimationKey,
                    onBarClick = { date ->
                        navController.navigate("statistics_detail/$date")
                    }
                )
            }

            composable(
                route = "statistics_detail/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date").orEmpty()
                val detailStateFlow = remember(date) {
                    timerViewModel.dailyStatisticsDetailState(date)
                }
                val detailState by detailStateFlow.collectAsState()

                StatisticsDetailScreen(
                    innerPadding = innerPadding,
                    state = detailState,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingScreen(
                    innerPadding = innerPadding,
                    viewModel = timerViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        AnimatedVisibility(
            visible = !timerState.isRunning && currentRoute?.startsWith("statistics_detail") != true,
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
                    if (route == "statistics") {
                        statisticsScrollAnimationKey += 1
                    }
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

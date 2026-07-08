package com.wakerep.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wakerep.app.ui.AlarmListViewModel
import com.wakerep.app.ui.screens.AddEditAlarmScreen
import com.wakerep.app.ui.screens.AlarmListScreen
import com.wakerep.app.ui.screens.OnboardingScreen
import com.wakerep.app.ui.screens.PaywallScreen
import com.wakerep.app.ui.screens.SettingsScreen
import com.wakerep.app.ui.theme.Background
import com.wakerep.app.ui.theme.WakerepTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding")
private val ONBOARDING_KEY = booleanPreferencesKey("has_completed_onboarding")

private val TOP_LEVEL_ROUTES = setOf("alarms", "settings")

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op: alarms still fire without it, just without a heads-up banner while foregrounded elsewhere */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val app = application as WakerepApplication

        setContent {
            WakerepTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Background) {
                    var hasCompletedOnboarding by remember { mutableStateOf<Boolean?>(null) }

                    LaunchedEffect(Unit) {
                        hasCompletedOnboarding = onboardingDataStore.data.first()[ONBOARDING_KEY] ?: false
                    }

                    when (hasCompletedOnboarding) {
                        null -> Unit
                        false -> OnboardingScreen(onFinished = {
                            lifecycleScope.launch {
                                onboardingDataStore.edit { it[ONBOARDING_KEY] = true }
                                hasCompletedOnboarding = true
                            }
                        })
                        true -> AppNavHost(app)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppNavHost(app: WakerepApplication) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val isSubscribed by app.billingManager.isSubscribed.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (currentRoute in TOP_LEVEL_ROUTES) {
                NavigationBar(containerColor = Background) {
                    NavigationBarItem(
                        selected = currentRoute == "alarms",
                        onClick = { navController.navigate("alarms") { launchSingleTop = true } },
                        icon = { Icon(Icons.Filled.Alarm, contentDescription = null) },
                        label = { Text("Alarms") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == "settings",
                        onClick = { navController.navigate("settings") { launchSingleTop = true } },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text("Settings") },
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "alarms",
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable("alarms") {
                val viewModel: AlarmListViewModel = viewModel(
                    factory = AlarmListViewModel.Factory(app.alarmRepository, context)
                )
                AlarmListScreen(
                    viewModel = viewModel,
                    isSubscribed = isSubscribed,
                    onAddAlarm = { navController.navigate("alarm/new") },
                    onEditAlarm = { alarm -> navController.navigate("alarm/${alarm.id}") },
                    onBlockedByPaywall = { navController.navigate("paywall") },
                )
            }
            composable(
                route = "alarm/{alarmId}",
                arguments = listOf(navArgument("alarmId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val alarmId = backStackEntry.arguments?.getString("alarmId")
                val viewModel: AlarmListViewModel = viewModel(
                    factory = AlarmListViewModel.Factory(app.alarmRepository, context)
                )
                val alarms by viewModel.alarms.collectAsStateWithLifecycle()
                val existingAlarm = if (alarmId == "new") null else alarms.firstOrNull { it.id == alarmId }
                AddEditAlarmScreen(
                    existingAlarm = existingAlarm,
                    onSave = { alarm ->
                        if (existingAlarm == null) viewModel.add(alarm) else viewModel.update(alarm)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() },
                )
            }
            composable("paywall") {
                PaywallScreen(billingManager = app.billingManager, onDismiss = { navController.popBackStack() })
            }
            composable("settings") {
                SettingsScreen(billingManager = app.billingManager, onUpgrade = { navController.navigate("paywall") })
            }
        }
    }
}

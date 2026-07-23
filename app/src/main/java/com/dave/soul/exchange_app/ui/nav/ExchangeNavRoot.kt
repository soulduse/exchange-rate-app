package com.dave.soul.exchange_app.ui.nav

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.ads.AdsManager
import com.dave.soul.exchange_app.ads.BannerAd
import com.dave.soul.exchange_app.ui.alerts.AlertsScreen
import com.dave.soul.exchange_app.ui.calculator.CalculatorScreen
import com.dave.soul.exchange_app.ui.detail.DetailScreen
import com.dave.soul.exchange_app.ui.home.HomeScreen
import com.dave.soul.exchange_app.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val CALCULATOR = "calculator"
    const val ALERTS = "alerts"
    const val SETTINGS = "settings"
    const val DETAIL = "detail/{code}"

    fun detail(code: String) = "detail/$code"
}

private data class Tab(val route: String, val icon: ImageVector, val labelRes: Int)

@Composable
fun ExchangeNavRoot(adsManager: AdsManager) {
    val navController = rememberNavController()
    val tabs = listOf(
        Tab(Routes.HOME, Icons.Filled.CurrencyExchange, R.string.tab_home),
        Tab(Routes.CALCULATOR, Icons.Filled.Calculate, R.string.tab_calculator),
        Tab(Routes.ALERTS, Icons.Filled.NotificationsActive, R.string.tab_alerts),
        Tab(Routes.SETTINGS, Icons.Filled.Settings, R.string.tab_settings),
    )
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in tabs.map { it.route }) {
                Column {
                    BannerAd(unitId = adsManager.bannerUnitId)
                    NavigationBar {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = null) },
                                label = { Text(stringResource(tab.labelRes)) },
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(onCurrencyClick = { code -> navController.navigate(Routes.detail(code)) })
            }
            composable(Routes.CALCULATOR) { CalculatorScreen() }
            composable(Routes.ALERTS) { AlertsScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
            composable(Routes.DETAIL) { entry ->
                val code = entry.arguments?.getString("code").orEmpty()
                val activity = LocalContext.current as? Activity
                LaunchedEffect(code) { activity?.let(adsManager::onDetailEnter) }
                DetailScreen(currencyCode = code, onBack = { navController.popBackStack() })
            }
        }
    }
}

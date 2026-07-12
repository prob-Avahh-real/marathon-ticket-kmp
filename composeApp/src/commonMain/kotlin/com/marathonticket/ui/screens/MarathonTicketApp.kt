// MarathonTicketApp.kt — 主导航 + 页面壳
package com.marathonticket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

enum class Screen {
    HOME, DETAIL, ORDERS
}

@Composable
fun MarathonTicketApp() {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var detailEventId by remember { mutableStateOf<String?>(null) }
    var showingOrders by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (!showingOrders && currentScreen != Screen.DETAIL) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentScreen == Screen.HOME,
                        onClick = {
                            currentScreen = Screen.HOME
                            showingOrders = false
                        },
                        icon = { Icon(Icons.Filled.DirectionsRun, "赛事") },
                        label = { Text("赛事") }
                    )
                    NavigationBarItem(
                        selected = showingOrders,
                        onClick = {
                            currentScreen = Screen.ORDERS
                            showingOrders = true
                        },
                        icon = { Icon(Icons.Outlined.ConfirmationNumber, "报名") },
                        label = { Text("我的报名") }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                currentScreen == Screen.DETAIL && detailEventId != null -> {
                    DetailScreen(
                        eventId = detailEventId!!,
                        onBack = {
                            currentScreen = Screen.HOME
                            detailEventId = null
                        },
                        onGrabSuccess = {
                            currentScreen = Screen.ORDERS
                            showingOrders = true
                        }
                    )
                }
                showingOrders || currentScreen == Screen.ORDERS -> {
                    OrdersScreen(onBack = {
                        currentScreen = Screen.HOME
                        showingOrders = false
                    })
                }
                else -> {
                    HomeScreen(
                        onNavigateToDetail = { id ->
                            detailEventId = id
                            currentScreen = Screen.DETAIL
                        }
                    )
                }
            }
        }
    }
}

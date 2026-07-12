// App.kt — 马拉松抢票 KMP 入口
package com.marathonticket

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.marathonticket.ui.screens.MarathonTicketApp

@Composable
fun App() {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFFE53935)
    )) {
        MarathonTicketApp()
    }
}

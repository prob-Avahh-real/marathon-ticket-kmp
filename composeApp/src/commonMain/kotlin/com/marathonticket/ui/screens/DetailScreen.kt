// DetailScreen.kt — 赛事详情 + 抢票入口
package com.marathonticket.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.marathonticket.data.EventRepository
import com.marathonticket.model.EventStatus
import com.marathonticket.model.MarathonEvent
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onGrabSuccess: () -> Unit
) {
    var event by remember { mutableStateOf<MarathonEvent?>(null) }
    var showGrabDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(eventId) {
        scope.launch {
            event = EventRepository.getEventById(eventId)
        }
    }

    event?.let { evt ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(evt.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                        }
                    }
                )
            },
            bottomBar = {
                Surface(
                    shadowElevation = 8.dp,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("¥${evt.price.toInt()}", color = MaterialTheme.colorScheme.primary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text("报名费", color = Color.Gray, fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Button(
                            onClick = { showGrabDialog = true },
                            enabled = evt.canGrab,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (evt.canGrab) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                if (evt.canGrab) "立即抢票" else evt.status.label,
                                fontSize = 18.sp, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
                // 封面
                Box(Modifier.fillMaxWidth().height(250.dp)) {
                    AsyncImage(model = evt.coverImage, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
                    Text(evt.name, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }

                // 基本信息
                Column(Modifier.padding(20.dp)) {
                    Row {
                        InfoItem(Icons.Outlined.CalendarToday, "比赛日期", evt.eventDate.take(10))
                        Spacer(Modifier.width(16.dp))
                        InfoItem(Icons.Outlined.AccessTime, "开抢时间", evt.ticketSaleTime.replace("T", " "))
                    }
                    Spacer(Modifier.height(16.dp))
                    Row {
                        InfoItem(Icons.Outlined.LocationOn, "起点", evt.location)
                        Spacer(Modifier.width(16.dp))
                        InfoItem(Icons.Outlined.Straighten, "距离", evt.distance)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row {
                        InfoItem(Icons.Outlined.ConfirmationNumber, "名额", "${evt.remainingQuota}/${evt.totalQuota}")
                        evt.organizerName?.let {
                            Spacer(Modifier.width(16.dp))
                            InfoItem(Icons.Outlined.Business, "主办方", it)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // 报名进度
                    Text("报名进度", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { evt.soldPercent.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp)),
                        color = progressColor(evt.soldPercent),
                        trackColor = Color.Gray.copy(alpha = 0.15f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("已报名 ${evt.totalQuota - evt.remainingQuota} 人", color = Color.Gray, fontSize = 12.sp)
                }

                // 赛事亮点
                if (evt.highlights.isNotEmpty()) {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        Text("赛事亮点", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            evt.highlights.take(4).forEach { h ->
                                AssistChip(onClick = {}, label = { Text(h, fontSize = 13.sp) })
                            }
                        }
                    }
                }

                // 描述
                Column(Modifier.padding(20.dp)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text("赛事介绍", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(evt.description, color = Color.Gray.copy(alpha = 0.85f), lineHeight = 22.sp)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showGrabDialog) {
        GrabDialog(
            event = event,
            onDismiss = { showGrabDialog = false },
            onSuccess = {
                showGrabDialog = false
                onGrabSuccess()
            }
        )
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(Modifier.width(4.dp))
            Text(label, color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.W600, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

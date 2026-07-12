// HomeScreen.kt — 首页：赛事列表 + Tab 分类 + 事件卡片
package com.marathonticket.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
fun HomeScreen(onNavigateToDetail: (String) -> Unit) {
    var events by remember { mutableStateOf<List<MarathonEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            events = EventRepository.getEvents()
            isLoading = false
        }
    }

    fun refresh() {
        scope.launch {
            isLoading = true
            events = EventRepository.getEvents()
            isLoading = false
        }
    }

    val tabEvents = when (selectedTab) {
        0 -> events.filter { it.status == EventStatus.ON_SALE }
        1 -> events.filter { it.status == EventStatus.UPCOMING }
        2 -> events.filter { it.status == EventStatus.SOLD_OUT || it.status == EventStatus.ENDED }
        else -> emptyList()
    }

    Column(Modifier.fillMaxSize()) {
        // Hero banner
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            AsyncImage(
                model = "https://picsum.photos/seed/marathon_banner/800/400",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
            )
            Text(
                "马拉松抢票",
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold
            )
        }

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            listOf("报名中", "即将开始", "已结束").forEachIndexed { i, title ->
                Tab(
                    selected = selectedTab == i,
                    onClick = { selectedTab = i },
                    text = { Text(title) }
                )
            }
        }

        // Content
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (tabEvents.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.DirectionsRun, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        when (selectedTab) { 0 -> "暂无报名中的赛事"; 1 -> "暂无即将开始的赛事"; else -> "暂无已结束的赛事" },
                        color = Color.Gray
                    )
                }
            }
        } else {
            PullToRefreshBox(isRefreshing = isLoading, onRefresh = { refresh() }) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tabEvents) { event ->
                        EventCard(event = event, onClick = { onNavigateToDetail(event.id) })
                    }
                }
            }
        }
    }
}

// ── 赛事卡片 ───────────────────────────────────────────

@Composable
fun EventCard(event: MarathonEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column {
            // 封面图
            Box {
                AsyncImage(
                    model = event.coverImage,
                    contentDescription = event.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
                // 状态标签
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor(event.status)
                ) {
                    Text(
                        event.status.label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold
                    )
                }
                // 售罄遮罩
                if (event.isSoldOut) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.SentimentDissatisfied, "已售罄", tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                }
            }

            // 信息区
            Column(Modifier.padding(16.dp)) {
                Text(event.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(event.location, color = Color.Gray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(4.dp))

                Row {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(event.eventDate.take(10), color = Color.Gray, fontSize = 13.sp)
                    Spacer(Modifier.width(16.dp))
                    Icon(Icons.Outlined.Straighten, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(event.distance, color = Color.Gray, fontSize = 13.sp)
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¥${event.price.toInt()}", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    if (event.totalQuota > 0) {
                        Text("${(event.soldPercent * 100).toInt()}% 已抢", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                if (event.totalQuota > 0) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { event.soldPercent.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                        color = progressColor(event.soldPercent),
                        trackColor = Color.Gray.copy(alpha = 0.15f)
                    )
                }
            }
        }
    }
}

fun statusColor(status: EventStatus): Color = when (status) {
    EventStatus.UPCOMING -> Color(0xFFFF9800)
    EventStatus.ON_SALE -> Color(0xFF4CAF50)
    EventStatus.SOLD_OUT -> Color(0xFFF44336)
    EventStatus.ENDED -> Color.Gray
}

fun progressColor(percent: Double): Color = when {
    percent < 0.5 -> Color(0xFF4CAF50)
    percent < 0.8 -> Color(0xFFFF9800)
    else -> Color(0xFFF44336)
}

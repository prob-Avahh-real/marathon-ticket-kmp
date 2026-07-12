// OrdersScreen.kt — 我的报名/订单列表
package com.marathonticket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.marathonticket.data.OrderRepository
import com.marathonticket.model.Order
import com.marathonticket.model.OrderStatus
import kotlinx.coroutines.launch

@Composable
fun OrdersScreen(onBack: () -> Unit) {
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            orders = OrderRepository.getMyOrders()
            isLoading = false
        }
    }

    fun refresh() {
        scope.launch {
            isLoading = true
            orders = OrderRepository.getMyOrders()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的报名") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            orders.isEmpty() -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Gray.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text("还没有报名任何赛事", color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text("去首页看看有哪些赛事可以报名吧", color = Color.Gray.copy(alpha = 0.6f), fontSize = 14.sp)
                }
            }
            else -> {
                PullToRefreshBox(isRefreshing = isLoading, onRefresh = { refresh() }, modifier = Modifier.padding(padding)) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders) { order ->
                            OrderCard(order = order, onCancel = {
                                scope.launch {
                                    OrderRepository.cancelOrder(order.id)
                                    orders = OrderRepository.getMyOrders()
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, onCancel: () -> Unit) {
    var showCancelDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // 封面
            AsyncImage(
                model = order.eventCover,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(order.eventName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text("${order.eventDate.take(10)} · ${order.distance}", color = Color.Gray, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = orderStatusBg(order.status)
                    ) {
                        Text(
                            order.status.label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = orderStatusColor(order.status),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W600
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text("¥${order.price.toInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            if (order.status == OrderStatus.PAID) {
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { showCancelDialog = true }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))) {
                    Text("取消")
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("取消报名") },
            text = { Text("确定要取消「${order.eventName}」的报名吗？") },
            confirmButton = {
                TextButton(onClick = { showCancelDialog = false; onCancel() }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))) {
                    Text("确定取消")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("再想想") }
            }
        )
    }
}

fun orderStatusColor(status: OrderStatus): Color = when (status) {
    OrderStatus.PENDING -> Color(0xFFFF9800)
    OrderStatus.PAID -> Color(0xFF4CAF50)
    OrderStatus.CANCELLED -> Color.Gray
    OrderStatus.REFUNDED -> Color.Gray
}

fun orderStatusBg(status: OrderStatus): Color = when (status) {
    OrderStatus.PENDING -> Color(0xFFFF9800).copy(alpha = 0.12f)
    OrderStatus.PAID -> Color(0xFF4CAF50).copy(alpha = 0.12f)
    OrderStatus.CANCELLED -> Color.Gray.copy(alpha = 0.1f)
    OrderStatus.REFUNDED -> Color.Gray.copy(alpha = 0.1f)
}

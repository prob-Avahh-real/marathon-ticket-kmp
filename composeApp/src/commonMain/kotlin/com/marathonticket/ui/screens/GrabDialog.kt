// GrabDialog.kt — 抢票弹窗：倒计时 + 抢票动画
package com.marathonticket.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.marathonticket.data.EventRepository
import com.marathonticket.data.OrderRepository
import com.marathonticket.model.EventStatus
import com.marathonticket.model.MarathonEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class GrabPhase { COUNTDOWN, READY, GRABBING, SUCCESS, FAILED, SOLD_OUT }

@Composable
fun GrabDialog(
    event: MarathonEvent?,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    if (event == null) return

    var phase by remember { mutableStateOf(GrabPhase.COUNTDOWN) }
    var timeLeft by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var orderId by remember { mutableStateOf("") }
    var ticketCode by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val pulseAnim = rememberInfiniteTransition()
    val pulseScale by pulseAnim.animateFloat(initialValue = 1f, targetValue = 1.08f, animationSpec = infiniteRepeatable(tween(800, easing = EaseInOut), RepeatMode.Reverse))

    // Countdown logic
    LaunchedEffect(event) {
        try {
            val saleTime = kotlinx.datetime.Instant.parse(event.ticketSaleTime)
            while (true) {
                val now = kotlinx.datetime.Clock.System.now()
                val diff = saleTime - now
                if (diff <= kotlin.time.Duration.ZERO) {
                    phase = if (event.canGrab) GrabPhase.READY else GrabPhase.SOLD_OUT
                    break
                }
                val totalSeconds = diff.inWholeSeconds
                val days = totalSeconds / 86400
                val hours = (totalSeconds % 86400) / 3600
                val mins = (totalSeconds % 3600) / 60
                val secs = totalSeconds % 60
                timeLeft = "${days}天 ${"%02d".format(hours)}:${"%02d".format(mins)}:${"%02d".format(secs)}"
                delay(100)
            }
        } catch (_: Exception) {
            phase = if (event.canGrab) GrabPhase.READY else GrabPhase.SOLD_OUT
        }
    }

    fun grab() {
        if (phase != GrabPhase.READY) return
        phase = GrabPhase.GRABBING
        message = "正在抢票..."
        scope.launch {
            try {
                val result = EventRepository.grabTicket(event.id)
                if (result.success) {
                    val order = OrderRepository.addOrder(
                        eventId = event.id, eventName = event.name,
                        eventCover = event.coverImage, eventDate = event.eventDate,
                        distance = event.distance, price = event.price
                    )
                    orderId = order.id
                    ticketCode = order.ticketCode ?: ""
                    phase = GrabPhase.SUCCESS
                    message = result.message
                } else {
                    phase = GrabPhase.FAILED
                    message = result.message
                }
            } catch (e: Exception) {
                phase = GrabPhase.FAILED
                message = "网络异常，请重试"
            }
        }
    }

    Dialog(
        onDismissRequest = { if (phase != GrabPhase.GRABBING) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                when (phase) {
                    GrabPhase.COUNTDOWN -> {
                        Text(event.name, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Text("${event.distance} · ¥${event.price.toInt()}", color = Color.Gray)
                        Spacer(Modifier.height(32.dp))
                        Text("距离抢票开始", color = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Text(timeLeft, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(24.dp))
                        Text("请保持页面打开", color = Color.Gray.copy(alpha = 0.6f), fontSize = 13.sp)
                    }

                    GrabPhase.READY -> {
                        Icon(Icons.Filled.AccessAlarm, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFF4CAF50))
                        Spacer(Modifier.height(16.dp))
                        Text("可以抢票了！", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        Spacer(Modifier.height(8.dp))
                        Text("${event.name} · ${event.distance}", color = Color.Gray)
                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { grab() },
                            modifier = Modifier.scale(pulseScale).width(200.dp).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("立即抢票", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    GrabPhase.GRABBING -> {
                        CircularProgressIndicator(modifier = Modifier.size(80.dp), strokeWidth = 4.dp)
                        Spacer(Modifier.height(24.dp))
                        Text(message, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("请耐心等待，不要关闭页面", color = Color.Gray)
                    }

                    GrabPhase.SUCCESS -> {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color(0xFF4CAF50))
                        Spacer(Modifier.height(16.dp))
                        Text("抢票成功！", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        Spacer(Modifier.height(8.dp))
                        Text(message, color = Color.Gray)
                        if (orderId.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Surface(color = Color(0xFF4CAF50).copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("订单号：$orderId", fontWeight = FontWeight.W600)
                                    Text("参赛码：${ticketCode.ifEmpty { "待分配" }}", color = Color.Gray)
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onSuccess, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                            Text("完成", fontSize = 18.sp)
                        }
                    }

                    GrabPhase.FAILED -> {
                        Icon(Icons.Filled.SentimentDissatisfied, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color(0xFFE53935))
                        Spacer(Modifier.height(16.dp))
                        Text("抢票失败", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                        Spacer(Modifier.height(8.dp))
                        Text(message, color = Color.Gray)
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = { grab() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)), shape = RoundedCornerShape(24.dp)) {
                            Text("再试一次", fontSize = 18.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = onDismiss) { Text("返回") }
                    }

                    GrabPhase.SOLD_OUT -> {
                        Icon(Icons.Filled.SentimentDissatisfied, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Text("已售罄", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text("该赛事名额已被抢光", color = Color.Gray.copy(alpha = 0.7f))
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("返回") }
                    }
                }
            }
        }
    }
}

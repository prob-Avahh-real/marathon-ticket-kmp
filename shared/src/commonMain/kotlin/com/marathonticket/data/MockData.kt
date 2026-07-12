// MockData.kt — 赛事 Mock 数据 + 抢票服务
package com.marathonticket.data

import com.marathonticket.model.*
import kotlinx.coroutines.delay
import kotlinx.datetime.*
import kotlin.random.Random

object EventRepository {
    private val mockEvents = listOf(
        MarathonEvent(
            id = "bj2025", name = "2025 北京马拉松",
            coverImage = "https://picsum.photos/seed/marathon1/800/400",
            location = "天安门广场", city = "北京",
            eventDate = "2025-11-02T07:30:00", ticketSaleTime = "2025-07-15T10:00:00",
            distance = "全程 42.195km", price = 200.0,
            totalQuota = 30000, remainingQuota = 28450,
            status = EventStatus.ON_SALE,
            description = "北京马拉松是中国最高水平的马拉松赛事之一，起于天安门广场，终于奥林匹克公园。",
            highlights = listOf("国际田联金标赛事", "天安门起跑", "鸟巢终点"),
            organizerName = "中国田径协会"
        ),
        MarathonEvent(
            id = "sh2025", name = "2025 上海国际马拉松",
            coverImage = "https://picsum.photos/seed/marathon2/800/400",
            location = "外滩金牛广场", city = "上海",
            eventDate = "2025-11-30T07:00:00", ticketSaleTime = "2025-08-01T10:00:00",
            distance = "全程 42.195km", price = 150.0,
            totalQuota = 38000, remainingQuota = 12100,
            status = EventStatus.ON_SALE,
            description = "上海国际马拉松赛是世界田联白金标赛事。",
            highlights = listOf("白金标赛事", "外滩起跑"),
            organizerName = "上海市体育局"
        ),
        MarathonEvent(
            id = "xm2026", name = "2026 厦门马拉松",
            coverImage = "https://picsum.photos/seed/marathon3/800/400",
            location = "厦门国际会展中心", city = "厦门",
            eventDate = "2026-01-04T07:30:00", ticketSaleTime = "2025-10-01T10:00:00",
            distance = "全程 42.195km", price = 180.0,
            totalQuota = 35000, remainingQuota = 0,
            status = EventStatus.SOLD_OUT,
            description = "厦门马拉松。",
            highlights = listOf("最美赛道"), organizerName = "厦门市体育局"
        )
    )

    suspend fun getEvents(): List<MarathonEvent> {
        delay(600)
        return mockEvents
    }

    suspend fun getEventById(id: String): MarathonEvent? {
        delay(300)
        return mockEvents.firstOrNull { it.id == id }
    }

    data class GrabResult(val success: Boolean, val orderId: String? = null, val message: String)

    suspend fun grabTicket(eventId: String): GrabResult {
        delay((100 + Random.nextInt(400)).toLong())
        val event = mockEvents.firstOrNull { it.id == eventId } ?: return GrabResult(false, message = "赛事未找到")
        val success = Random.nextInt(100) < 80
        return if (success && event.status == EventStatus.ON_SALE && !event.isSoldOut) {
            GrabResult(true, orderId = "ORD${Clock.System.now().toEpochMilliseconds()}", message = "抢票成功！")
        } else {
            GrabResult(false, message = if (event.isSoldOut) "已售罄" else "手慢了，已被抢光")
        }
    }
}

object OrderRepository {
    private val _orders = mutableListOf<Order>()

    suspend fun getMyOrders(): List<Order> {
        delay(400)
        _orders.sortByDescending { it.createdAt }
        return _orders.toList()
    }

    suspend fun addOrder(
        eventId: String, eventName: String, eventCover: String,
        eventDate: String, distance: String, price: Double
    ): Order {
        delay(200)
        val now = Clock.System.now()
        val order = Order(
            id = "ORD${now.toEpochMilliseconds()}",
            eventId = eventId, eventName = eventName, eventCover = eventCover,
            eventDate = eventDate, distance = distance, price = price,
            status = OrderStatus.PAID, createdAt = now.toString(),
            ticketCode = "T${now.toEpochMilliseconds()}"
        )
        _orders.add(order)
        return order
    }

    suspend fun cancelOrder(orderId: String) {
        delay(300)
        val idx = _orders.indexOfFirst { it.id == orderId }
        if (idx != -1) {
            _orders[idx] = _orders[idx].copy(status = OrderStatus.CANCELLED)
        }
    }
}

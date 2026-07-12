// Order.kt — 订单/票务模型
package com.marathonticket.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    val eventId: String,
    val eventName: String,
    val eventCover: String,
    val eventDate: String,  // ISO 8601
    val distance: String,
    val price: Double,
    val status: OrderStatus,
    val createdAt: String,  // ISO 8601
    val ticketCode: String? = null,
    val bibNumber: String? = null
)

@Serializable
enum class OrderStatus {
    PENDING, PAID, CANCELLED, REFUNDED;

    val label: String get() = when (this) {
        PENDING -> "待支付"
        PAID -> "已报名"
        CANCELLED -> "已取消"
        REFUNDED -> "已退款"
    }
}

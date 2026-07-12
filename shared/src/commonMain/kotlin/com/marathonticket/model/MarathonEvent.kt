// MarathonEvent.kt — 马拉松赛事模型
package com.marathonticket.model

import kotlinx.serialization.Serializable

@Serializable
data class MarathonEvent(
    val id: String,
    val name: String,
    val coverImage: String,
    val location: String,
    val city: String,
    val eventDate: String,       // ISO 8601
    val ticketSaleTime: String,  // ISO 8601
    val distance: String,
    val price: Double,
    val totalQuota: Int,
    val remainingQuota: Int,
    val status: EventStatus,
    val description: String,
    val highlights: List<String> = emptyList(),
    val organizerName: String? = null,
    val routeMapUrl: String? = null
) {
    val soldPercent: Double get() =
        if (totalQuota > 0) (totalQuota - remainingQuota).toDouble() / totalQuota else 0.0

    val isSoldOut: Boolean get() = remainingQuota <= 0

    val canGrab: Boolean get() = status == EventStatus.ON_SALE && !isSoldOut
}

@Serializable
enum class EventStatus {
    UPCOMING, ON_SALE, SOLD_OUT, ENDED;

    val label: String get() = when (this) {
        UPCOMING -> "即将开抢"
        ON_SALE -> "报名中"
        SOLD_OUT -> "已售罄"
        ENDED -> "已结束"
    }
}

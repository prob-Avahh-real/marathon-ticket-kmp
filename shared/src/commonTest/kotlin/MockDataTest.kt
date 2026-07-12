package com.marathonticket.data

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MockDataTest {

    @Test
    fun eventRepository_returns_events() = runTest {
        val events = EventRepository.getEvents()
        assertTrue(events.isNotEmpty(), "Should have mock events")
    }

    @Test
    fun orderRepository_add_and_retrieve() = runTest {
        val order = OrderRepository.addOrder(
            eventId = "test", eventName = "Test Event",
            eventCover = "", eventDate = "2025-01-01",
            distance = "42km", price = 100.0
        )
        assertNotNull(order.id)
        val orders = OrderRepository.getMyOrders()
        assertTrue(orders.isNotEmpty())
    }
}

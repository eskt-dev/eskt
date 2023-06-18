package dev.eskt.store

import dev.eskt.store.wellknown.car.CarProducedEvent
import dev.eskt.store.wellknown.car.CarSoldEvent
import dev.eskt.store.wellknown.car.CarStreamType
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AppendStreamTest {
    @Test
    @JsName("test1")
    fun `given no events - when appending events on a stream - event is added`() {
        // given
        val storage = InMemoryStorage()

        // when
        val eventStore = InMemoryEventStore(storage)
        val event1 = CarProducedEvent(vin = "123", producer = 1, make = "kia", model = "rio")
        eventStore
            .withStreamType(CarStreamType)
            .appendStream(
                streamId = "car-123",
                expectedVersion = 0,
                events = listOf(
                    event1,
                ),
            )
            .unwrap()

        // then
        assertEquals(event1, storage.events[0].event)
        assertEquals(event1, storage.eventsByStreamId["car-123"]!![0])
    }

    @Test
    @JsName("test2")
    fun `given 2 event from different streams - when appending events on a stream - event is added`() {
        // given
        val storage = InMemoryStorage()
        storage.add(CarStreamType, "car-123", CarProducedEvent(vin = "123", producer = 1, make = "kia", model = "rio"))
        storage.add(CarStreamType, "car-456", CarProducedEvent(vin = "456", producer = 1, make = "kia", model = "rio"))

        // when
        val event1 = CarSoldEvent(seller = 1, buyer = 2, 2500.00f)
        val eventStore = InMemoryEventStore(storage)
        eventStore
            .withStreamType(CarStreamType)
            .appendStream(
                streamId = "car-123",
                expectedVersion = 1,
                events = listOf(
                    event1,
                ),
            )
            .unwrap()

        // then
        assertEquals(event1, storage.events[2].event)
        assertEquals(event1, storage.eventsByStreamId["car-123"]!![1])
    }

    @Test
    @JsName("test3")
    fun `given 1 event from same stream - when appending event out of order - append is rejected`() {
        // given
        val storage = InMemoryStorage()
        storage.add(CarStreamType, "car-123", CarProducedEvent(vin = "123", producer = 1, make = "kia", model = "rio"))

        // when
        val event1 = CarSoldEvent(seller = 1, buyer = 2, 2500.00f)
        val eventStore = InMemoryEventStore(storage)
        val result = eventStore
            .withStreamType(CarStreamType)
            .appendStream(
                streamId = "car-123",
                expectedVersion = 2, // this would create a gap if accepted
                events = listOf(
                    event1,
                ),
            )

        // then
        val expected = Result.Failure(AppendFailure.ExpectedVersionMismatch(currentVersion = 1, expectedVersion = 2))
        assertEquals(expected, result)
    }
}

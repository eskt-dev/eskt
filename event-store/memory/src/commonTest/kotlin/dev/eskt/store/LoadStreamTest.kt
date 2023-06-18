package dev.eskt.store

import dev.eskt.store.wellknown.car.CarProducedEvent
import dev.eskt.store.wellknown.car.CarSoldEvent
import dev.eskt.store.wellknown.car.CarStreamType
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LoadStreamTest {
    @Test
    @JsName("test1")
    fun `given no events - when loading events of a stream - list is empty`() {
        // given
        val storage = InMemoryStorage()

        // when
        val eventStore = InMemoryEventStore(storage)
        val events = eventStore
            .withStreamType(CarStreamType)
            .loadStream(
                streamId = "car-123",
            )
            .unwrap()

        // then
        assertEquals(0, events.size)
    }

    @Test
    @JsName("test2")
    fun `given 3 event from different streams - when loading one stream - correct events are loaded`() {
        // given
        val storage = InMemoryStorage()
        storage.add(CarStreamType, "car-123", CarProducedEvent(vin = "123", producer = 1, make = "kia", model = "rio"))
        storage.add(CarStreamType, "car-456", CarProducedEvent(vin = "456", producer = 1, make = "kia", model = "rio"))
        storage.add(CarStreamType, "car-123", CarSoldEvent(seller = 1, buyer = 2, 2500.00f))

        // when
        val eventStore = InMemoryEventStore(storage)

        (0..1).forEach { sinceVersion ->
            val events = eventStore
                .withStreamType(CarStreamType)
                .loadStream(
                    streamId = "car-123",
                    sinceVersion = sinceVersion,
                )
                .unwrap()

            // then
            assertEquals(2 - sinceVersion, events.size)
            assertEquals(
                expected = EventEnvelope(
                    streamType = CarStreamType,
                    streamId = "car-123",
                    version = 2,
                    position = 3,
                    metadata = emptyMap(),
                    event = CarSoldEvent(seller = 1, buyer = 2, 2500.00f),
                ),
                actual = events.last(),
            )
        }
    }
}

package dev.eskt.store.test

import dev.eskt.store.EventEnvelope
import dev.eskt.store.EventStore
import dev.eskt.store.storage.api.Storage
import dev.eskt.store.test.w.car.CarProducedEvent
import dev.eskt.store.test.w.car.CarSoldEvent
import dev.eskt.store.test.w.car.CarStreamType
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("DuplicatedCode")
public open class LoadStreamTest<R : Storage, S : EventStore>(
    public val storageFactory: () -> R,
    public val storeFactory: (storage: R) -> S,
) {
    @Test
    @JsName("test1")
    public fun `given no events - when loading events of a stream - list is empty`() {
        // given
        val storage = storageFactory()

        // when
        val eventStore = storeFactory(storage)
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
    public fun `given 3 event from different streams - when loading one stream - correct events are loaded`() {
        // given
        val storage = storageFactory()
        storage.add(CarStreamType, "car-123", 1, CarProducedEvent(vin = "123", producer = 1, make = "kia", model = "rio"))
        storage.add(CarStreamType, "car-456", 1, CarProducedEvent(vin = "456", producer = 1, make = "kia", model = "rio"))
        storage.add(CarStreamType, "car-123", 2, CarSoldEvent(seller = 1, buyer = 2, 2500.00f))

        // when
        val eventStore = storeFactory(storage)

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

package dev.eskt.store.test

import dev.eskt.store.api.EventStore
import dev.eskt.store.storage.api.Storage
import dev.eskt.store.test.w.car.CarEvent
import dev.eskt.store.test.w.car.CarProducedEvent
import dev.eskt.store.test.w.car.CarSoldEvent
import dev.eskt.store.test.w.car.CarStreamType
import dev.eskt.store.test.w.driver.DriverRegisteredEvent
import dev.eskt.store.test.w.driver.DriverStreamType
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("DuplicatedCode")
public open class LoadEventTest<R : Storage, S : EventStore, F : StreamTestFactory<R, S>>(
    protected val factory: F,
) {
    @Test
    @JsName("test1")
    public fun `given 3 event from different streams - when loading events - correct events are loaded`() {
        // given
        val storage = factory.newStorage()
        storage.add(CarStreamType, car1StreamId, 1, CarProducedEvent(vin = "123", producer = 1, make = "kia", model = "rio"))
        storage.add(CarStreamType, car2StreamId, 1, CarProducedEvent(vin = "456", producer = 1, make = "kia", model = "rio"))
        storage.add(CarStreamType, car1StreamId, 2, CarSoldEvent(seller = 1, buyer = 2, 2500.00f))

        // when
        val eventStore = factory.newEventStore(storage)
        (0..3L).forEach { sincePosition ->
            val eventEnvelopes = eventStore.loadEventBatch(sincePosition)

            // then
            assertEquals(3 - sincePosition.toInt(), eventEnvelopes.size)
            assertTrue(eventEnvelopes.map { it.event }.all { it is CarEvent })
        }
    }

    @Test
    @JsName("test2")
    public fun `given 4 event from different streams - when loading events - correct events are loaded`() {
        // given
        val storage = factory.newStorage()
        storage.add(CarStreamType, car1StreamId, 1, CarProducedEvent(vin = "123", producer = 1, make = "kia", model = "rio"))
        storage.add(DriverStreamType, driver1StreamId, 1, DriverRegisteredEvent("123445", "Driver 1"))
        storage.add(CarStreamType, car2StreamId, 1, CarProducedEvent(vin = "456", producer = 1, make = "kia", model = "rio"))
        storage.add(CarStreamType, car1StreamId, 2, CarSoldEvent(seller = 1, buyer = 2, 2500.00f))

        // when
        val eventStore = factory.newEventStore(storage)
        val eventEnvelopes = eventStore.loadEventBatch(0, streamType = CarStreamType)

        // then
        assertEquals(3, eventEnvelopes.size)
        assertTrue(eventEnvelopes.map { it.event }.all { it is CarEvent })
    }
}

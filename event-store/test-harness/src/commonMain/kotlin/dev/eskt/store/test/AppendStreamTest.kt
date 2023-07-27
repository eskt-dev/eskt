package dev.eskt.store.test

import dev.eskt.store.AppendFailure
import dev.eskt.store.EventStore
import dev.eskt.store.Result
import dev.eskt.store.storage.api.Storage
import dev.eskt.store.test.w.car.CarProducedEvent
import dev.eskt.store.test.w.car.CarSoldEvent
import dev.eskt.store.test.w.car.CarStreamType
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("DuplicatedCode")
public abstract class AppendStreamTest<R : Storage, S : EventStore>(
    public val storageFactory: () -> R,
    public val storeFactory: (storage: R) -> S,
) {
    @Test
    @JsName("test1")
    public fun `given no events - when appending events on a stream - event is added`() {
        // given
        val storage = storageFactory()

        // when
        val eventStore = storeFactory(storage)
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
        assertEquals(event1, storage.getEvent(1).event)
        assertEquals(event1, storage.getStreamEvent(CarStreamType, "car-123", 1).event)
    }

    @Test
    @JsName("test2")
    public fun `given 2 event from different streams - when appending events on a stream - event is added`() {
        // given
        val storage = storageFactory()
        storage.add(CarStreamType, "car-123", 1, CarProducedEvent(vin = "123", producer = 1, make = "kia", model = "rio"))
        storage.add(CarStreamType, "car-456", 1, CarProducedEvent(vin = "456", producer = 1, make = "kia", model = "rio"))

        // when
        val event1 = CarSoldEvent(seller = 1, buyer = 2, 2500.00f)
        val eventStore = storeFactory(storage)
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
        assertEquals(event1, storage.getEvent(3).event)
        assertEquals(event1, storage.getStreamEvent(CarStreamType, "car-123", 2).event)
    }

    @Test
    @JsName("test3")
    public fun `given 1 event from same stream - when appending event with already existing version - append is rejected`() {
        // given
        val storage = storageFactory()
        storage.add(CarStreamType, "car-123", 1, CarProducedEvent(vin = "123", producer = 1, make = "kia", model = "rio"))
        storage.add(CarStreamType, "car-123", 2, CarSoldEvent(seller = 1, buyer = 2, 2500.00f))

        // when
        val event1 = CarSoldEvent(seller = 1, buyer = 3, 2500.00f)
        val eventStore = storeFactory(storage)
        val result = eventStore
            .withStreamType(CarStreamType)
            .appendStream(
                streamId = "car-123",
                expectedVersion = 1,
                events = listOf(
                    event1,
                ),
            )

        // then
        val expected = Result.Failure(AppendFailure.ExpectedVersionMismatch(currentVersion = 2, expectedVersion = 1))
        assertEquals(expected, result)
    }

    @Test
    @JsName("test4")
    public fun `given 1 event from same stream - when appending event out of order - append is rejected`() {
        // given
        val storage = storageFactory()
        storage.add(CarStreamType, "car-123", 1, CarProducedEvent(vin = "123", producer = 1, make = "kia", model = "rio"))

        // when
        val event1 = CarSoldEvent(seller = 1, buyer = 2, 2500.00f)
        val eventStore = storeFactory(storage)
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

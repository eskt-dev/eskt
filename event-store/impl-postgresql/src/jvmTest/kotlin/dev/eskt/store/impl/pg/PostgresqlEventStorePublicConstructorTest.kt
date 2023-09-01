package dev.eskt.store.impl.pg

import dev.eskt.store.test.w.car.CarStreamType
import org.junit.Test

internal class PostgresqlEventStorePublicConstructorTest {
    @Test
    fun `public constructor creates event store and allow withStreamType to be called`() {
        val eventStore = PostgresqlEventStore(generateTestConnectionConfig(), "new_events") {
            registerStreamType(CarStreamType)
        }

        eventStore.withStreamType(CarStreamType)
    }
}

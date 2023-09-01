package dev.eskt.store.impl.pg

import com.zaxxer.hikari.HikariDataSource
import dev.eskt.store.test.w.car.CarStreamType
import org.junit.Test

internal class PostgresqlEventStorePublicConstructorTest {
    @Test
    fun `public constructor creates event store and allow withStreamType to be called`() {
        val dataSource = HikariDataSource(generateTestConnectionConfig().copy(database = "postgres").toHikariConfig())
        val eventStore = PostgresqlEventStore(dataSource, "new_events") {
            registerStreamType(CarStreamType)
        }

        eventStore.withStreamType(CarStreamType)
    }
}

package dev.eskt.store.impl.pg

import dev.eskt.store.impl.common.string.serialization.DefaultEventMetadataSerializer
import dev.eskt.store.test.StreamTestFactory
import dev.eskt.store.test.w.car.CarStreamType
import dev.eskt.store.test.w.driver.DriverStreamType

@OptIn(ExperimentalStdlibApi::class)
internal class PostgresqlStreamTestFactory : StreamTestFactory<PostgresqlStorage, PostgresqlEventStore>() {
    internal val connectionConfig = generateTestConnectionConfig()

    private val config
        get() = PostgresqlConfig(
            registeredTypes = listOf(
                CarStreamType,
                DriverStreamType,
            ),
            eventMetadataSerializer = DefaultEventMetadataSerializer,
            dataSource = connectionConfig.dataSource(closeables),
            eventTable = "event",
        )

    override fun createStorage(): PostgresqlStorage {
        return PostgresqlStorage(config)
    }

    override fun createEventStore(storage: PostgresqlStorage): PostgresqlEventStore {
        return PostgresqlEventStore(config, storage)
    }
}

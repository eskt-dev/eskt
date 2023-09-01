package dev.eskt.store.impl.pg

import dev.eskt.store.impl.common.string.serialization.DefaultEventMetadataSerializer
import dev.eskt.store.test.StreamTestFactory
import dev.eskt.store.test.w.car.CarStreamType

internal class PostgresqlStreamTestFactory : StreamTestFactory<PostgresqlStorage, PostgresqlEventStore>() {
    internal val config = PostgresqlConfig(
        registeredTypes = listOf(
            CarStreamType,
        ),
        eventMetadataSerializer = DefaultEventMetadataSerializer,
        connectionConfig = generateTestConnectionConfig(),
        eventTable = "event",
    )

    override fun createStorage(): PostgresqlStorage {
        return PostgresqlStorage(config)
    }

    override fun createEventStore(storage: PostgresqlStorage): PostgresqlEventStore {
        return PostgresqlEventStore(config, storage)
    }
}

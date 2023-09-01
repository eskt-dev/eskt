package dev.eskt.store.impl.pg

import dev.eskt.store.api.EventStore
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StreamTypeHandler

public class PostgresqlEventStore internal constructor(
    config: PostgresqlConfig,
    private val storage: PostgresqlStorage = PostgresqlStorage(config),
) : EventStore {
    public constructor(
        connectionConfig: ConnectionConfig,
        eventTable: String,
        block: PostgresqlConfigBuilder.() -> Unit,
    ) : this(
        PostgresqlConfigBuilder(connectionConfig, eventTable)
            .apply(block)
            .build(),
    )

    override fun <I, E> withStreamType(type: StreamType<I, E>): StreamTypeHandler<I, E> {
        return dev.eskt.store.impl.common.base.StreamTypeHandler(type, storage)
    }

    public fun close() {
        storage.close()
    }
}

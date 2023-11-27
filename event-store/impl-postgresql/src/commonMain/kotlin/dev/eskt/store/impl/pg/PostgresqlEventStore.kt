package dev.eskt.store.impl.pg

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventStore
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StreamTypeHandler

public class PostgresqlEventStore internal constructor(
    internal val config: PostgresqlConfig,
    private val storage: PostgresqlStorage = PostgresqlStorage(config),
) : EventStore {
    public constructor(
        dataSource: DataSource,
        eventTable: String,
        block: PostgresqlConfigBuilder.() -> Unit,
    ) : this(
        PostgresqlConfigBuilder(dataSource, eventTable)
            .apply(block)
            .build(),
    )

    override fun loadEventBatch(sincePosition: Long, batchSize: Int): List<EventEnvelope<Any, Any>> {
        return storage.loadEventBatch(sincePosition, batchSize)
    }

    override fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int, streamType: StreamType<I, E>): List<EventEnvelope<I, E>> {
        return storage.loadEventBatch(sincePosition, batchSize, streamType)
    }

    override fun <I, E> withStreamType(type: StreamType<I, E>): StreamTypeHandler<I, E> {
        return dev.eskt.store.impl.common.base.StreamTypeHandler(type, storage)
    }
}

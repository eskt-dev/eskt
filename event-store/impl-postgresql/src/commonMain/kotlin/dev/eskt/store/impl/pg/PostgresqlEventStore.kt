package dev.eskt.store.impl.pg

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventStore
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StreamTypeHandler

public class PostgresqlEventStore internal constructor(
    private val config: PostgresqlConfig,
    private val storage: PostgresqlStorage = PostgresqlStorage(config),
) : EventStore {
    public constructor(
        dataSource: DataSource,
        eventTable: String,
        eventWriteTable: String = eventTable,
        block: PostgresqlConfigBuilder.() -> Unit,
    ) : this(
        PostgresqlConfigBuilder(dataSource, eventTable, eventWriteTable)
            .apply(block)
            .build(),
    )

    override val registeredTypes: Set<StreamType<*, *>> = config.registeredTypes.toSet()

    override fun loadEventBatch(sincePosition: Long, batchSize: Int): List<EventEnvelope<Any, Any>> {
        return storage.loadEventBatch(sincePosition, batchSize)
    }

    override fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int, streamType: StreamType<E, I>): List<EventEnvelope<E, I>> {
        return storage.loadEventBatch(sincePosition, batchSize, streamType)
    }

    override fun <E, I> withStreamType(type: StreamType<E, I>): StreamTypeHandler<E, I> {
        return dev.eskt.store.impl.common.base.StreamTypeHandler(type, storage)
    }
}

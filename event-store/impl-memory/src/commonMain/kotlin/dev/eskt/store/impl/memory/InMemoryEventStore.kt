package dev.eskt.store.impl.memory

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventStore
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StreamTypeHandler

public class InMemoryEventStore internal constructor(
    config: InMemoryConfig,
    private val storage: InMemoryStorage = InMemoryStorage(config),
) : EventStore {
    public constructor(block: InMemoryConfigBuilder.() -> Unit) : this(
        InMemoryConfigBuilder()
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

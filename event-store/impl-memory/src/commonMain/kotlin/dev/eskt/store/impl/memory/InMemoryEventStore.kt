package dev.eskt.store.impl.memory

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventStore
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StreamTypeHandler
import dev.eskt.store.storage.api.Storage

public class InMemoryEventStore internal constructor(
    private val config: InMemoryConfig,
    private val storageImpl: StorageImpl,
    private val storage: Storage = when (storageImpl) {
        StorageImpl.CopyOnWrite -> CopyOnWriteInMemoryStorage(config)
        StorageImpl.Synchronized -> SynchronizedInMemoryStorage(config)
    },
) : EventStore {
    public constructor(
        storageImpl: StorageImpl = StorageImpl.Synchronized,
        block: InMemoryConfigBuilder.() -> Unit,
    ) : this(
        InMemoryConfigBuilder()
            .apply(block)
            .build(),
        storageImpl,
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

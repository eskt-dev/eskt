package dev.eskt.store.impl.fs

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventStore
import dev.eskt.store.api.StreamType

public class FileSystemEventStore internal constructor(
    config: FileSystemConfig,
    private val storage: FileSystemStorage = FileSystemStorage(config),
) : EventStore {
    public constructor(basePath: String, block: FileSystemConfigBuilder.() -> Unit) : this(
        FileSystemConfigBuilder(basePath)
            .apply(block)
            .build(),
    )

    override fun loadEventBatch(sincePosition: Long, batchSize: Int): List<EventEnvelope<Any, Any>> {
        return storage.loadEventBatch(sincePosition, batchSize)
    }

    override fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int, streamType: StreamType<I, E>): List<EventEnvelope<I, E>> {
        return storage.loadEventBatch(sincePosition, batchSize, streamType)
    }

    override fun <I, E> withStreamType(type: StreamType<I, E>): dev.eskt.store.api.StreamTypeHandler<I, E> {
        return dev.eskt.store.impl.common.base.StreamTypeHandler(type, storage)
    }
}

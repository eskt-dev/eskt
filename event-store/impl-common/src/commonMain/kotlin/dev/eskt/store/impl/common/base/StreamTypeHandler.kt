package dev.eskt.store.impl.common.base

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StreamTypeHandler
import dev.eskt.store.api.StreamVersionMismatchException
import dev.eskt.store.storage.api.Storage
import dev.eskt.store.storage.api.StorageVersionMismatchException

public class StreamTypeHandler<I, E>(
    override val streamType: StreamType<I, E>,
    private val storage: Storage,
) : StreamTypeHandler<I, E> {
    override fun loadStream(streamId: I, sinceVersion: Int): List<EventEnvelope<I, E>> {
        return storage.getStreamEvents(streamId, sinceVersion)
    }

    override fun appendStream(streamId: I, expectedVersion: Int, events: List<E>, metadata: EventMetadata): Int {
        try {
            storage.add(streamType, streamId, expectedVersion, events, metadata)
        } catch (e: StorageVersionMismatchException) {
            throw StreamVersionMismatchException(e.currentVersion, e.expectedVersion)
        }
        return expectedVersion + events.size
    }
}

package dev.eskt.store.impl.common.base.blocking

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StreamVersionMismatchException
import dev.eskt.store.storage.api.StorageVersionMismatchException
import dev.eskt.store.storage.api.blocking.Storage

public class StreamTypeHandler<E, I>(
    override val streamType: StreamType<E, I>,
    private val storage: Storage,
) : dev.eskt.store.api.blocking.StreamTypeHandler<E, I> {
    override fun loadStream(streamId: I, sinceVersion: Int): List<EventEnvelope<E, I>> {
        return storage.useStreamEvents(streamType, streamId, sinceVersion) { stream ->
            stream.toList()
        }
    }

    override fun <R> useStream(streamId: I, sinceVersion: Int, consume: (Sequence<EventEnvelope<E, I>>) -> R): R {
        return storage.useStreamEvents(streamType, streamId, sinceVersion, consume)
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

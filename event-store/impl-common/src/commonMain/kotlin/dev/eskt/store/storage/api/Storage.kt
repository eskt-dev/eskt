package dev.eskt.store.storage.api

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.StreamType

public interface Storage {
    @Throws(StorageVersionMismatchException::class)
    public fun <I, E> add(streamType: StreamType<I, E>, streamId: I, expectedVersion: Int, events: List<E>, metadata: EventMetadata)

    public fun <I, E> getStreamEvents(streamId: I, sinceVersion: Int): List<EventEnvelope<I, E>>

    public fun <I, E> getEventByPosition(position: Long): EventEnvelope<I, E>

    public fun loadEventBatch(sincePosition: Long, batchSize: Int): List<EventEnvelope<Any, Any>>

    public fun <I, E> loadEventBatch(sincePosition: Long, batchSize: Int, streamType: StreamType<I, E>): List<EventEnvelope<I, E>>

    public fun <I, E> getEventByStreamVersion(streamId: I, version: Int): EventEnvelope<I, E>
}

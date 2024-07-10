package dev.eskt.store.storage.api

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.StreamType

public interface Storage {
    @Throws(StorageVersionMismatchException::class)
    public fun <E, I> add(streamType: StreamType<E, I>, streamId: I, expectedVersion: Int, events: List<E>, metadata: EventMetadata)

    public fun <E, I> getStreamEvents(streamType: StreamType<E, I>, streamId: I, sinceVersion: Int): List<EventEnvelope<E, I>>

    public fun <E, I> getEventByPosition(position: Long): EventEnvelope<E, I>

    public fun loadEventBatch(sincePosition: Long, batchSize: Int): List<EventEnvelope<Any, Any>>

    public fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int, streamType: StreamType<E, I>): List<EventEnvelope<E, I>>

    public fun <E, I> getEventByStreamVersion(streamType: StreamType<E, I>, streamId: I, version: Int): EventEnvelope<E, I>
}

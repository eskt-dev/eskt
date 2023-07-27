package dev.eskt.store.storage.api

import dev.eskt.store.EventMetadata
import dev.eskt.store.StreamType

public interface Storage {
    @Throws(ExpectedVersionMismatch::class)
    public fun <I, E> add(streamType: StreamType<I, E>, streamId: I, expectedVersion: Int, events: List<E>, metadata: EventMetadata)

    @Throws(ExpectedVersionMismatch::class)
    public fun <I, E> add(streamType: StreamType<I, E>, streamId: I, version: Int, event: E, metadata: EventMetadata = emptyMap())

    public fun <I, E> getEvent(streamType: StreamType<I, E>, position: Long): E

    public fun <I, E> getStreamEvent(streamType: StreamType<I, E>, streamId: I, version: Int): E
}

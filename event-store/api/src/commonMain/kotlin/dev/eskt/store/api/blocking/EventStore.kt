package dev.eskt.store.api.blocking

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.StreamType

public interface EventStore : dev.eskt.store.api.EventStore {
    public fun <E, I> loadEventBatch(
        sincePosition: Long,
        batchSize: Int = 1000,
    ): List<EventEnvelope<E, I>>

    public fun <E, I> loadEventBatch(
        sincePosition: Long,
        batchSize: Int = 1000,
        streamType: StreamType<E, I>,
    ): List<EventEnvelope<E, I>>

    public fun <E, I> withStreamType(
        type: StreamType<E, I>,
    ): StreamTypeHandler<E, I>
}

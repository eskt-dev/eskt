package dev.eskt.store.api

public interface EventStore {
    public val registeredTypes: Set<StreamType<*, *>>

    public fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int = 1000): List<EventEnvelope<E, I>>

    public fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int = 1000, streamType: StreamType<E, I>): List<EventEnvelope<E, I>>

    public fun <E, I> withStreamType(type: StreamType<E, I>): StreamTypeHandler<E, I>
}

package dev.eskt.store.api

public interface EventStore {
    public fun loadEventBatch(sincePosition: Long, batchSize: Int = 1000): List<EventEnvelope<Any, Any>>

    public fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int = 1000, streamType: StreamType<I, E>): List<EventEnvelope<I, E>>

    public fun <I, E> withStreamType(type: StreamType<I, E>): StreamTypeHandler<I, E>
}

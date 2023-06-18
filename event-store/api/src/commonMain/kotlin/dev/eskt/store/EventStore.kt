package dev.eskt.store

public interface EventStore {
    public fun <I, E> withStreamType(type: StreamType<I, E>): StreamTypeHandler<I, E>
}

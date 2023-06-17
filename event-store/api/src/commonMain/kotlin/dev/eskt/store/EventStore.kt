package dev.eskt.store

interface EventStore {
    fun <I, E> withStreamType(type: StreamType<I, E>): StreamTypeHandler<I, E>
}

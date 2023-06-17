package dev.eskt

interface EventStore {
    fun <I, E> withStreamType(type: StreamType<I, E>): StreamTypeHandler<I, E>
}

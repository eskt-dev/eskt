package dev.eskt.store

interface StreamTypeHandler<I, E> {
    fun appendStream(streamId: I, expectedVersion: Int, events: List<E>): AppendResult
}

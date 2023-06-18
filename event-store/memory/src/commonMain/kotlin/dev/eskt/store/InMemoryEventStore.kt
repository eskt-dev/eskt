package dev.eskt.store

class InMemoryEventStore internal constructor(
    private val storage: InMemoryStorage,
) : EventStore {
    constructor() : this(InMemoryStorage())

    override fun <I, E> withStreamType(type: StreamType<I, E>): StreamTypeHandler<I, E> {
        return InMemoryStreamTypeHandler(type, storage)
    }
}

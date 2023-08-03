package dev.eskt.store.impl.memory

import dev.eskt.store.api.EventStore
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StreamTypeHandler

public class InMemoryEventStore internal constructor(
    private val storage: InMemoryStorage,
) : EventStore {
    public constructor() : this(InMemoryStorage())

    override fun <I, E> withStreamType(type: StreamType<I, E>): StreamTypeHandler<I, E> {
        return InMemoryStreamTypeHandler(type, storage)
    }
}

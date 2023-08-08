package dev.eskt.store.impl.memory

import dev.eskt.store.api.EventStore
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StreamTypeHandler

public class InMemoryEventStore internal constructor(
    config: InMemoryConfig,
    private val storage: InMemoryStorage = InMemoryStorage(config),
) : EventStore {
    public constructor(block: InMemoryConfigBuilder.() -> Unit) : this(
        InMemoryConfigBuilder()
            .apply(block)
            .build(),
    )

    override fun <I, E> withStreamType(type: StreamType<I, E>): StreamTypeHandler<I, E> {
        return dev.eskt.store.impl.common.base.StreamTypeHandler(type, storage)
    }
}

package dev.eskt.store.impl.memory

import dev.eskt.store.api.StreamType

internal class InMemoryConfig(
    val registeredTypes: List<StreamType<*, *>>,
)

public class InMemoryConfigBuilder {
    private val registeredTypes = mutableListOf<StreamType<*, *>>()

    public fun <I, E, T> registerStreamType(streamType: T) where T : StreamType<I, E> {
        registeredTypes += streamType
    }

    internal fun build(): InMemoryConfig = InMemoryConfig(
        registeredTypes = registeredTypes,
    )
}

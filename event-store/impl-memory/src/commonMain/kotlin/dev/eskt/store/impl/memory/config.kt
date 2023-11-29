package dev.eskt.store.impl.memory

import dev.eskt.store.api.StreamType

internal class InMemoryConfig(
    val registeredTypes: List<StreamType<*, *>>,
)

public class InMemoryConfigBuilder {
    private val registeredTypes = mutableListOf<StreamType<*, *>>()

    public fun <E, I, T> registerStreamType(streamType: T) where T : StreamType<E, I> {
        registeredTypes += streamType
    }

    internal fun build(): InMemoryConfig = InMemoryConfig(
        registeredTypes = registeredTypes,
    )
}

package dev.eskt.store.impl.memory

import dev.eskt.store.api.BinarySerializableStreamType
import dev.eskt.store.api.StreamType

internal class InMemoryConfig(
    val registeredTypes: List<BinarySerializableStreamType<*, *>>,
)

public class InMemoryConfigBuilder {
    private val registeredTypes = mutableListOf<BinarySerializableStreamType<*, *>>()

    public fun <I, E, T> registerStreamType(streamType: T) where T : StreamType<I, E>, T : BinarySerializableStreamType<I, E> {
        registeredTypes += streamType as BinarySerializableStreamType<*, *>
    }

    internal fun build(): InMemoryConfig = InMemoryConfig(
        registeredTypes = registeredTypes,
    )
}

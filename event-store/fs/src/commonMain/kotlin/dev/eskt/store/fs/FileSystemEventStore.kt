package dev.eskt.store.fs

import dev.eskt.store.api.BinarySerializableStreamType
import dev.eskt.store.api.EventStore
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StreamTypeHandler
import okio.Path.Companion.toPath

public class FileSystemEventStore internal constructor(
    private val storage: FileSystemStorage,
    block: InitContext.() -> Unit,
) : EventStore {
    public constructor(basePath: String, block: InitContext.() -> Unit) : this(
        FileSystemStorage(basePath.toPath(true)),
        block,
    )

    private val registeredTypes = mutableMapOf<String, BinarySerializableStreamType<*, *>>()

    init {
        InitContext().block()
    }

    public inner class InitContext {
        public fun <I, E, T> registerStreamType(streamType: T) where T : StreamType<I, E>, T : BinarySerializableStreamType<I, E> {
            registeredTypes[streamType.id] = streamType as BinarySerializableStreamType<*, *>
        }
    }

    override fun <I, E> withStreamType(type: StreamType<I, E>): StreamTypeHandler<I, E> {
        return FileSystemStreamTypeHandler(type, storage)
    }
}

package dev.eskt.store.impl.fs

import dev.eskt.store.api.BinarySerializableStreamType
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.EventStore
import dev.eskt.store.api.Serializer
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

        public fun eventMetadataSerializer(eventMetadataSerializer: Serializer<EventMetadata, ByteArray>) {
            storage.eventMetadataSerializer = eventMetadataSerializer
        }
    }

    override fun <I, E> withStreamType(type: StreamType<I, E>): StreamTypeHandler<I, E> {
        return FileSystemStreamTypeHandler(type, storage)
    }
}

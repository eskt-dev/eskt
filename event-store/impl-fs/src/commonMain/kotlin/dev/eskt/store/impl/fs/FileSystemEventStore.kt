package dev.eskt.store.impl.fs

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.EventStore
import dev.eskt.store.api.Serializer
import dev.eskt.store.api.StreamType

public class FileSystemEventStore internal constructor(
    private val config: FileSystemConfig,
    private val storage: FileSystemStorage = FileSystemStorage(config),
) : EventStore {
    public constructor(basePath: String, block: FileSystemConfigBuilder.() -> Unit) : this(
        FileSystemConfigBuilder(basePath)
            .apply(block)
            .build(),
    )

    override val registeredTypes: Set<StreamType<*, *>> = config.registeredTypes.toSet()

    public val basePath: String = storage.basePath.toString()

    public fun initStorage() {
        storage.initStorage()
    }

    override fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int): List<EventEnvelope<E, I>> {
        return storage.loadEventBatch(sincePosition, batchSize)
    }

    override fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int, streamType: StreamType<E, I>): List<EventEnvelope<E, I>> {
        return storage.loadEventBatch(sincePosition, batchSize, streamType)
    }

    override fun <E, I> withStreamType(type: StreamType<E, I>): dev.eskt.store.api.StreamTypeHandler<E, I> {
        return dev.eskt.store.impl.common.base.StreamTypeHandler(type, storage)
    }

    @Suppress("UNCHECKED_CAST")
    public fun <E, I> getPayloadSerializer(streamType: StreamType<E, I>): Serializer<E, ByteArray> {
        return config.payloadSerializers[streamType] as Serializer<E, ByteArray>
    }

    @Suppress("UNCHECKED_CAST")
    public fun <E, I> getIdSerializer(streamType: StreamType<E, I>): Serializer<I, String> {
        return config.idSerializers[streamType] as Serializer<I, String>
    }

    public fun getMetadataSerializer(): Serializer<EventMetadata, ByteArray> {
        return config.eventMetadataSerializer
    }
}

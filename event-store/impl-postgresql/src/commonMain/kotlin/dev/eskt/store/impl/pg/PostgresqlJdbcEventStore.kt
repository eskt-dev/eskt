package dev.eskt.store.impl.pg

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.Serializer
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.blocking.EventStore
import dev.eskt.store.api.blocking.StreamTypeHandler

public class PostgresqlJdbcEventStore internal constructor(
    private val config: PostgresqlConfig<DataSource>,
    private val storage: PostgresqlJdbcStorage = PostgresqlJdbcStorage(config),
) : EventStore {
    override val registeredTypes: Set<StreamType<*, *>> = config.registeredTypes.toSet()

    override fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int): List<EventEnvelope<E, I>> {
        return storage.loadEventBatch(sincePosition, batchSize)
    }

    override fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int, streamType: StreamType<E, I>): List<EventEnvelope<E, I>> {
        return storage.loadEventBatch(sincePosition, batchSize, streamType)
    }

    override fun <E, I> withStreamType(type: StreamType<E, I>): StreamTypeHandler<E, I> {
        return dev.eskt.store.impl.common.base.blocking.StreamTypeHandler(type, storage)
    }

    public val dataSource: DataSource
        get() = config.dataSource

    @Suppress("UNCHECKED_CAST")
    public fun <E, I> getPayloadSerializer(streamType: StreamType<E, I>): Serializer<E, String> {
        return config.payloadSerializers[streamType] as Serializer<E, String>
    }

    @Suppress("UNCHECKED_CAST")
    public fun <E, I> getIdSerializer(streamType: StreamType<E, I>): Serializer<I, String> {
        return config.idSerializers[streamType] as Serializer<I, String>
    }

    public fun getMetadataSerializer(): Serializer<EventMetadata, String> {
        return config.eventMetadataSerializer
    }
}

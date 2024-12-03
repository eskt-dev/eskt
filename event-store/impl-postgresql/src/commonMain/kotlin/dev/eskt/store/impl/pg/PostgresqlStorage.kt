package dev.eskt.store.impl.pg

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.Serializer
import dev.eskt.store.api.StreamType
import dev.eskt.store.storage.api.Storage

internal class PostgresqlStorage(
    internal val config: PostgresqlConfig,
) : Storage {
    private val registeredTypes = config.registeredTypes.associateBy { it.id }
    private val eventMetadataSerializer = config.eventMetadataSerializer

    private val databaseAdapter = DatabaseAdapter(config.dataSource)

    override fun <E, I> add(streamType: StreamType<E, I>, streamId: I, expectedVersion: Int, events: List<E>, metadata: EventMetadata) {
        if (streamType.id !in registeredTypes) throw IllegalStateException("Unregistered type: $streamType")
        val serializedId = streamType.stringIdSerializer.serialize(streamId)
        val entries = events.mapIndexed { index, event ->
            DatabaseEntry(
                type = streamType.id,
                id = serializedId,
                version = expectedVersion + index + 1,
                eventPayload = streamType.stringEventSerializer.serialize(event),
                metadataPayload = eventMetadataSerializer.serialize(metadata),
            )
        }

        databaseAdapter.persistEntries(serializedId, expectedVersion, entries, config.tableInfo)
    }

    override fun <E, I> getStreamEvents(streamType: StreamType<E, I>, streamId: I, sinceVersion: Int): List<EventEnvelope<E, I>> {
        if (streamType.id !in registeredTypes) throw IllegalStateException("Unregistered type: $streamType")
        val entries = databaseAdapter.getEntriesByStreamIdAndVersion(
            streamId = streamType.stringIdSerializer.serialize(streamId),
            sinceVersion = sinceVersion,
            tableInfo = config.tableInfo,
        )
        return entries.map { entry -> entry.toEventEnvelope() }
    }

    override fun <E, I, R> useStreamEvents(
        streamType: StreamType<E, I>,
        streamId: I,
        sinceVersion: Int,
        consume: (Sequence<EventEnvelope<E, I>>) -> R,
    ): R {
        if (streamType.id !in registeredTypes) throw IllegalStateException("Unregistered type: $streamType")
        return databaseAdapter.useEntriesByStreamIdAndVersion(
            streamId = streamType.stringIdSerializer.serialize(streamId),
            sinceVersion = sinceVersion,
            tableInfo = config.tableInfo,
            consume = { sequence: Sequence<DatabaseEntry> ->
                consume(sequence.map { it.toEventEnvelope() })
            },
        )
    }

    override fun <E, I> getEventByPosition(position: Long): EventEnvelope<E, I> {
        val entry = databaseAdapter.getEntryByPosition(position, config.tableInfo)
        return entry.toEventEnvelope()
    }

    override fun loadEventBatch(sincePosition: Long, batchSize: Int): List<EventEnvelope<Any, Any>> {
        val entries = databaseAdapter.getEntryBatch(
            sincePosition = sincePosition,
            batchSize = batchSize,
            tableInfo = config.tableInfo,
        )
        return entries.map { entry -> entry.toEventEnvelope() }
    }

    override fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int, streamType: StreamType<E, I>): List<EventEnvelope<E, I>> {
        val entries = databaseAdapter.getEntryBatch(
            sincePosition = sincePosition,
            batchSize = batchSize,
            type = streamType.id,
            tableInfo = config.tableInfo,
        )
        return entries.map { entry -> entry.toEventEnvelope() }
    }

    override fun <E, I> getEventByStreamVersion(streamType: StreamType<E, I>, streamId: I, version: Int): EventEnvelope<E, I> {
        if (streamType.id !in registeredTypes) throw IllegalStateException("Unregistered type: $streamType")
        val entry = databaseAdapter.getEntriesByStreamIdAndVersion(
            streamId = streamType.stringIdSerializer.serialize(streamId),
            sinceVersion = version - 1,
            limit = 1,
            tableInfo = config.tableInfo,
        ).single()
        return entry.toEventEnvelope()
    }

    private fun <E, I> DatabaseEntry.toEventEnvelope(): EventEnvelope<E, I> {
        val streamType = config.streamType<E, I, StreamType<E, I>>(type)
        return EventEnvelope(
            streamType = streamType,
            streamId = streamType.stringIdSerializer.deserialize(id),
            version = version,
            position = position,
            event = streamType.stringEventSerializer.deserialize(eventPayload),
            metadata = eventMetadataSerializer.deserialize(metadataPayload),
        )
    }

    @Suppress("UNCHECKED_CAST")
    private val <E, I> StreamType<E, I>.stringIdSerializer: Serializer<I, String>
        get() = config.idSerializers[this] as Serializer<I, String>

    @Suppress("UNCHECKED_CAST")
    private val <E, I> StreamType<E, I>.stringEventSerializer: Serializer<E, String>
        get() = config.payloadSerializers[this] as Serializer<E, String>

    internal class DatabaseEntry(
        val position: Long = -1,
        val type: String,
        val id: String,
        val version: Int,
        val eventPayload: String,
        val metadataPayload: String,
    )
}

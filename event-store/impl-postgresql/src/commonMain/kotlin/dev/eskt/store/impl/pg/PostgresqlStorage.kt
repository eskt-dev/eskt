package dev.eskt.store.impl.pg

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StringSerializableStreamType
import dev.eskt.store.storage.api.Storage

internal class PostgresqlStorage(
    internal val config: PostgresqlConfig,
) : Storage {
    private val registeredTypes = config.registeredTypes.associateBy { it.id }
    private val eventMetadataSerializer = config.eventMetadataSerializer

    private val databaseAdapter = DatabaseAdapter(config.dataSource)

    override fun <E, I> add(streamType: StreamType<E, I>, streamId: I, expectedVersion: Int, events: List<E>, metadata: EventMetadata) {
        if (streamType.id !in registeredTypes) throw IllegalStateException("Unregistered type: $streamType")
        streamType as StringSerializableStreamType<E, I>
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

    override fun <E, I> getStreamEvents(streamId: I, sinceVersion: Int): List<EventEnvelope<E, I>> {
        val entries = databaseAdapter.getEntriesByStreamIdAndVersion(
            streamId = streamId.toString(),
            sinceVersion = sinceVersion,
            tableInfo = config.tableInfo,
        )
        return entries.map { entry -> entry.toEventEnvelope() }
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

    override fun <E, I> getEventByStreamVersion(streamId: I, version: Int): EventEnvelope<E, I> {
        val entry = databaseAdapter.getEntriesByStreamIdAndVersion(
            streamId = streamId.toString(),
            sinceVersion = version - 1,
            limit = 1,
            tableInfo = config.tableInfo,
        ).single()
        return entry.toEventEnvelope()
    }

    private fun <E, I> DatabaseEntry.toEventEnvelope(): EventEnvelope<E, I> {
        val streamType = config.streamType<E, I, StringSerializableStreamType<E, I>>(type)
        return EventEnvelope(
            streamType = streamType,
            streamId = streamType.stringIdSerializer.deserialize(id),
            version = version,
            position = position,
            event = streamType.stringEventSerializer.deserialize(eventPayload),
            metadata = eventMetadataSerializer.deserialize(metadataPayload),
        )
    }

    internal class DatabaseEntry(
        val position: Long = -1,
        val type: String,
        val id: String,
        val version: Int,
        val eventPayload: String,
        val metadataPayload: String,
    )
}

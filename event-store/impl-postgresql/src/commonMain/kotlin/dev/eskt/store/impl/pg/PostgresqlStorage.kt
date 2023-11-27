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

    override fun <I, E> add(streamType: StreamType<I, E>, streamId: I, expectedVersion: Int, events: List<E>, metadata: EventMetadata) {
        if (streamType.id !in registeredTypes) throw IllegalStateException("Unregistered type: $streamType")
        streamType as StringSerializableStreamType<I, E>
        val entries = events.map {
            DatabaseEntry(
                type = streamType.id,
                id = streamType.stringIdSerializer.serialize(streamId),
                version = expectedVersion + 1,
                eventPayload = streamType.stringEventSerializer.serialize(it),
                metadataPayload = eventMetadataSerializer.serialize(metadata),
            )
        }

        databaseAdapter.persistEntries(entries, config.tableInfo)
    }

    override fun <I, E> getStreamEvents(streamId: I, sinceVersion: Int): List<EventEnvelope<I, E>> {
        val entries = databaseAdapter.getEntriesByStreamIdAndVersion(
            streamId = streamId.toString(),
            sinceVersion = sinceVersion,
            tableInfo = config.tableInfo,
        )
        return entries.map { entry -> entry.toEventEnvelope() }
    }

    override fun <I, E> getEventByPosition(position: Long): EventEnvelope<I, E> {
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

    override fun <I, E> loadEventBatch(sincePosition: Long, batchSize: Int, streamType: StreamType<I, E>): List<EventEnvelope<I, E>> {
        val entries = databaseAdapter.getEntryBatch(
            sincePosition = sincePosition,
            batchSize = batchSize,
            type = streamType.id,
            tableInfo = config.tableInfo,
        )
        return entries.map { entry -> entry.toEventEnvelope() }
    }

    override fun <I, E> getEventByStreamVersion(streamId: I, version: Int): EventEnvelope<I, E> {
        val entry = databaseAdapter.getEntriesByStreamIdAndVersion(
            streamId = streamId.toString(),
            sinceVersion = version - 1,
            limit = 1,
            tableInfo = config.tableInfo,
        ).single()
        return entry.toEventEnvelope()
    }

    private fun <E, I> DatabaseEntry.toEventEnvelope(): EventEnvelope<I, E> {
        val streamType = config.streamType<I, E, StringSerializableStreamType<I, E>>(type)
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

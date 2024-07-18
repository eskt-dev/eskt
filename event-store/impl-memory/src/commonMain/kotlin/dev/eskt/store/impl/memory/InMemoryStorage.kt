package dev.eskt.store.impl.memory

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.StreamType
import dev.eskt.store.storage.api.Storage
import dev.eskt.store.storage.api.StorageVersionMismatchException
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

@Suppress("UNCHECKED_CAST")
internal class InMemoryStorage(
    config: InMemoryConfig,
) : Storage {
    private val registeredTypes = config.registeredTypes.associateBy { it.id }

    private val events = mutableListOf<EventEnvelope<Any, Any>>()
    private val eventsByStreamId = mutableMapOf<Any, MutableList<EventEnvelope<Any, Any>>>()

    private val writeLock = reentrantLock()

    override fun <E, I> getStreamEvents(streamType: StreamType<E, I>, streamId: I, sinceVersion: Int): List<EventEnvelope<E, I>> {
        return events
            .filter { it.streamId == streamId }
            .drop(sinceVersion)
            .map { it as EventEnvelope<E, I> }
    }

    override fun <E, I> add(streamType: StreamType<E, I>, streamId: I, expectedVersion: Int, events: List<E>, metadata: EventMetadata) {
        if (streamType.id !in registeredTypes) throw IllegalStateException("Unregistered type: $streamType")
        writeLock.withLock {
            val streamEvents = eventsByStreamId[streamId as Any]
                ?: mutableListOf<EventEnvelope<Any, Any>>().also { eventsByStreamId[streamId] = it }
            if (streamEvents.size != expectedVersion) {
                throw StorageVersionMismatchException(currentVersion = streamEvents.size, expectedVersion = expectedVersion)
            }
            events.forEachIndexed { index, event ->
                val position = this.events.size + 1L
                val version = expectedVersion + index + 1

                val envelope = EventEnvelope(
                    streamType = streamType as StreamType<Any, Any>,
                    streamId = streamId as Any,
                    version = version,
                    position = position,
                    metadata = metadata,
                    event = event as Any,
                )
                this.events.add(envelope)
                streamEvents.add(envelope)
            }
        }
    }

    override fun <E, I> getEventByPosition(position: Long): EventEnvelope<E, I> = events[position.toInt() - 1] as EventEnvelope<E, I>

    override fun loadEventBatch(sincePosition: Long, batchSize: Int): List<EventEnvelope<Any, Any>> {
        return events.asSequence()
            .drop(sincePositionInt(sincePosition))
            .take(batchSize)
            .toList()
    }

    override fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int, streamType: StreamType<E, I>): List<EventEnvelope<E, I>> {
        return events.asSequence()
            .drop(sincePositionInt(sincePosition))
            .filter { it.streamType == streamType }
            .take(batchSize)
            .map { it as EventEnvelope<E, I> }
            .toList()
    }

    override fun <E, I> getEventByStreamVersion(streamType: StreamType<E, I>, streamId: I, version: Int): EventEnvelope<E, I> {
        val eventEnvelopes = eventsByStreamId[streamId as Any] as List<EventEnvelope<E, I>>
        return eventEnvelopes[version - 1]
    }

    private fun sincePositionInt(sincePosition: Long) = when {
        sincePosition > Int.MAX_VALUE -> throw IllegalStateException("In-memory implementation can't really support more than Int.MAX_VALUE entries")
        else -> sincePosition.toInt()
    }
}

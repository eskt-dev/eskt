package dev.eskt.store.impl.memory

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.StreamType
import dev.eskt.store.storage.api.Storage
import dev.eskt.store.storage.api.StorageVersionMismatchException
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

@Suppress("UNCHECKED_CAST")
internal class CopyOnWriteInMemoryStorage(
    config: InMemoryConfig,
) : Storage {
    private val registeredTypes = config.registeredTypes.associateBy { it.id }

    private var events = listOf<EventEnvelope<Any, Any>>()
    private var eventsByStreamId = mapOf<Any, List<EventEnvelope<Any, Any>>>()

    private val writeLock = reentrantLock()

    override fun <E, I> getStreamEvents(streamType: StreamType<E, I>, streamId: I, sinceVersion: Int): List<EventEnvelope<E, I>> {
        return streamEvents<E, I>(streamId).drop(sinceVersion)
    }

    override fun <E, I, R> useStreamEvents(
        streamType: StreamType<E, I>,
        streamId: I,
        sinceVersion: Int,
        consume: (Sequence<EventEnvelope<E, I>>) -> R,
    ): R {
        val sequence = streamEvents<E, I>(streamId).asSequence().drop(sinceVersion)
        return consume(sequence)
    }

    override fun <E, I> add(streamType: StreamType<E, I>, streamId: I, expectedVersion: Int, events: List<E>, metadata: EventMetadata) {
        if (streamType.id !in registeredTypes) throw IllegalStateException("Unregistered type: $streamType")
        writeLock.withLock {
            val streamEvents = streamEvents<Any, Any>(streamId as Any)
            if (streamEvents.size != expectedVersion) {
                throw StorageVersionMismatchException(currentVersion = streamEvents.size, expectedVersion = expectedVersion)
            }
            val envelopes = events.mapIndexed { index, event ->
                val position = this.events.size + index + 1L
                val version = expectedVersion + index + 1

                EventEnvelope(
                    streamType = streamType as StreamType<Any, Any>,
                    streamId = streamId as Any,
                    version = version,
                    position = position,
                    metadata = metadata,
                    event = event as Any,
                )
            }
            // CoW both data storages so we optimise for reads without synchronization
            this.eventsByStreamId += streamId to streamEvents + envelopes
            this.events += envelopes
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
        return streamEvents<E, I>(streamId)[version - 1]
    }

    private fun <E, I> streamEvents(streamId: I) = (eventsByStreamId[streamId as Any] ?: emptyList()) as List<EventEnvelope<E, I>>

    private fun sincePositionInt(sincePosition: Long) = when {
        sincePosition > Int.MAX_VALUE -> throw IllegalStateException("In-memory implementation can't really support more than Int.MAX_VALUE entries")
        else -> sincePosition.toInt()
    }
}

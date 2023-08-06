package dev.eskt.store.impl.memory

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.StreamType
import dev.eskt.store.storage.api.ExpectedVersionMismatch
import dev.eskt.store.storage.api.Storage
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

@Suppress("UNCHECKED_CAST")
internal class InMemoryStorage(
    config: InMemoryConfig,
) : Storage {
    private val events = mutableListOf<EventEnvelope<Any, Any>>()
    private val eventsByStreamId = mutableMapOf<Any, MutableList<EventEnvelope<Any, Any>>>()

    private val writeLock = reentrantLock()

    fun <I, E> instanceEnvelopes(streamType: StreamType<I, E>, streamId: I, sinceVersion: Int): List<EventEnvelope<I, E>> {
        return events
            .filter { it.streamType == streamType }
            .filter { it.streamId == streamId }
            .drop(sinceVersion)
            .map { it as EventEnvelope<I, E> }
    }

    override fun <I, E> add(streamType: StreamType<I, E>, streamId: I, expectedVersion: Int, events: List<E>, metadata: EventMetadata) {
        writeLock.withLock {
            val streamEvents = eventsByStreamId[streamId as Any]
                ?: mutableListOf<EventEnvelope<Any, Any>>().also { eventsByStreamId[streamId] = it }
            if (streamEvents.size != expectedVersion) {
                throw ExpectedVersionMismatch(currentVersion = streamEvents.size, expectedVersion = expectedVersion)
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

    override fun <I, E> add(streamType: StreamType<I, E>, streamId: I, version: Int, event: E, metadata: EventMetadata) {
        add(streamType, streamId, expectedVersion = version - 1, listOf(event), metadata)
    }

    override fun <I, E> getEvent(streamType: StreamType<I, E>, position: Long): EventEnvelope<I, E> = events[position.toInt() - 1] as EventEnvelope<I, E>

    override fun <I, E> getStreamEvent(streamType: StreamType<I, E>, streamId: I, version: Int): EventEnvelope<I, E> {
        val eventEnvelopes = eventsByStreamId[streamId as Any] as List<EventEnvelope<I, E>>
        return eventEnvelopes[version - 1]
    }
}

package dev.eskt.store

import dev.eskt.store.storage.api.Storage

@Suppress("UNCHECKED_CAST")
internal class InMemoryStorage : Storage {
    private val events = mutableListOf<EventEnvelope<Any, Any>>()
    private val eventsByStreamId = mutableMapOf<Any, MutableList<EventEnvelope<Any, Any>>>()

    fun <I, E> instanceEnvelopes(streamId: I, sinceVersion: Int): List<EventEnvelope<I, E>> {
        return events.filter { it.streamId == streamId }.drop(sinceVersion).map { it as EventEnvelope<I, E> }
    }

    fun <I, E> instanceEvents(instanceId: I): List<E> {
        val events = eventsByStreamId[instanceId as Any] ?: mutableListOf()
        return events as List<E>
    }

    override fun <I, E> add(streamType: StreamType<I, E>, streamId: I, version: Int, event: E) {
        val streamEvents = eventsByStreamId[streamId as Any]
            ?: mutableListOf<EventEnvelope<Any, Any>>().also { eventsByStreamId[streamId] = it }

        val position = events.size + 1L
        val version = streamEvents.size + 1

        val envelope = EventEnvelope(
            streamType = streamType as StreamType<Any, Any>,
            streamId = streamId as Any,
            version = version,
            position = position,
            metadata = emptyMap(),
            event = event as Any,
        )
        events.add(envelope)
        streamEvents.add(envelope)
    }

    override fun getEvent(position: Long) = events[position.toInt()]

    override fun getStreamEvent(streamId: Any, position: Long) = eventsByStreamId[streamId]!![position.toInt()]
}

package dev.eskt.store

@Suppress("UNCHECKED_CAST")
internal class InMemoryStorage {
    internal val events = mutableListOf<EventEnvelope<Any, Any>>()
    internal val eventsByStreamId = mutableMapOf<Any, MutableList<Any>>()

    fun <I, E> instanceEnvelopes(streamId: I, sinceVersion: Int): List<EventEnvelope<I, E>> {
        return events.filter { it.streamId == streamId }.drop(sinceVersion).map { it as EventEnvelope<I, E> }
    }

    fun <I, E> instanceEvents(instanceId: I): List<E> {
        val events = eventsByStreamId[instanceId as Any] ?: mutableListOf()
        return events as List<E>
    }

    fun <I, E> add(streamType: StreamType<I, E>, instanceId: I, event: E) {
        val streamEvents = eventsByStreamId[instanceId as Any]
            ?: mutableListOf<Any>().also { eventsByStreamId[instanceId] = it }
        streamEvents.add(event as Any)
        events.add(
            EventEnvelope(
                streamType = streamType as StreamType<Any, Any>,
                streamId = instanceId as Any,
                version = streamEvents.size,
                position = events.size + 1L,
                metadata = emptyMap(),
                event = event as Any,
            ),
        )
    }
}

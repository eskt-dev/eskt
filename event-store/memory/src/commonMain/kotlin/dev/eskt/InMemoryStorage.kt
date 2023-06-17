package dev.eskt

@Suppress("UNCHECKED_CAST")
internal class InMemoryStorage {
    internal val events = mutableListOf<Any>()
    internal val eventsByStreamId = mutableMapOf<Any, MutableList<Any>>()

    fun <I, E> instanceEvents(instanceId: I): List<E> {
        val events = eventsByStreamId[instanceId as Any] ?: mutableListOf()
        return events as List<E>
    }

    fun <I, E> add(instanceId: I, event: E) {
        events.add(event as Any)
        val streamEvents = eventsByStreamId[instanceId as Any]
            ?: mutableListOf<Any>().also { eventsByStreamId[instanceId] = it }
        streamEvents.add(event)
    }
}

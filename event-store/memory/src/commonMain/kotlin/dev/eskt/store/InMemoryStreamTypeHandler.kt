package dev.eskt.store

import dev.eskt.store.AppendResult
import dev.eskt.store.StreamTypeHandler

class InMemoryStreamTypeHandler<I, E> internal constructor(
    private val storage: InMemoryStorage,
) : StreamTypeHandler<I, E> {
    override fun appendStream(streamId: I, expectedVersion: Int, events: List<E>): AppendResult {
        val streamEvents: List<E> = storage.instanceEvents(streamId)
        if (streamEvents.size != expectedVersion) {
            return AppendResult.ExpectedVersionMismatch(streamEvents.size, expectedVersion)
        }
        events.forEach {
            storage.add(streamId, it)
        }
        return AppendResult.Appended(storage.instanceEvents<I, E>(streamId).size)
    }
}

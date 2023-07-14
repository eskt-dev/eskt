package dev.eskt.store.storage.api

import dev.eskt.store.EventEnvelope
import dev.eskt.store.StreamType

public interface Storage {
    public fun <I, E> add(streamType: StreamType<I, E>, streamId: I, version: Int, event: E)

    public fun getEvent(position: Long): EventEnvelope<Any, Any>

    public fun getStreamEvent(streamId: Any, position: Long): EventEnvelope<Any, Any>
}

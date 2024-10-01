package dev.eskt.arch.hex.port

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.StreamType

public interface MultiStreamTypeEventListener<E, I> : EventListener {
    public val streamTypes: List<StreamType<out E, I>>
    public fun listen(envelope: EventEnvelope<E, I>)
}

package dev.eskt.arch.hex.port

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.StreamType

public interface SingleStreamTypeEventListener<E, I> {
    public val id: String
    public val streamType: StreamType<E, I>
    public fun listen(envelope: EventEnvelope<E, I>)
}

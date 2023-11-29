package dev.eskt.store.test

import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.StreamType
import dev.eskt.store.storage.api.Storage

internal fun <E, I> Storage.add(streamType: StreamType<E, I>, streamId: I, version: Int, event: E, metadata: EventMetadata = emptyMap()) {
    add(streamType, streamId, version - 1, listOf(event), metadata)
}

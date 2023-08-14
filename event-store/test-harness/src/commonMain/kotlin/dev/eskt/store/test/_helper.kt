package dev.eskt.store.test

import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.StreamType
import dev.eskt.store.storage.api.Storage

internal fun <I, E> Storage.add(streamType: StreamType<I, E>, streamId: I, version: Int, event: E, metadata: EventMetadata = emptyMap()) {
    add(streamType, streamId, version - 1, listOf(event), metadata)
}

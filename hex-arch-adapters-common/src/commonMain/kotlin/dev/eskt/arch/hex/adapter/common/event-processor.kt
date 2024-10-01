package dev.eskt.arch.hex.adapter.common

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventStore
import dev.eskt.store.api.StreamType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

public suspend inline fun <E, I> EventStore.singleStreamTypeEventFlow(
    streamType: StreamType<E, I>,
    sincePosition: Long,
    batchSize: Int,
): Flow<EventEnvelope<E, I>> = channelFlow {
    var lastPosition = sincePosition
    while (true) {
        val eventBatch = loadEventBatch(lastPosition, batchSize, streamType = streamType)
        eventBatch.forEach {
            this.send(it)
            lastPosition = it.position
        }
        if (eventBatch.isEmpty()) delay(500)
        if (eventBatch.size < batchSize) delay(250)
    }
}

public suspend inline fun EventStore.multiStreamTypeEventFlow(
    sincePosition: Long,
    batchSize: Int,
): Flow<EventEnvelope<Any, Any>> = channelFlow {
    var lastPosition = sincePosition
    while (true) {
        val eventBatch = loadEventBatch(lastPosition, batchSize)
        eventBatch.forEach {
            this.send(it)
            lastPosition = it.position
        }
        if (eventBatch.isEmpty()) delay(500)
        if (eventBatch.size < batchSize) delay(250)
    }
}

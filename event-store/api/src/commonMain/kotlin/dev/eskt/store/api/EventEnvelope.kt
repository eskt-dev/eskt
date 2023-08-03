package dev.eskt.store.api

public data class EventEnvelope<I, out E>(
    val streamType: StreamType<I, @UnsafeVariance E>,
    val streamId: I,
    val version: Int,
    val position: Long,
    val metadata: EventMetadata,
    val event: E,
)

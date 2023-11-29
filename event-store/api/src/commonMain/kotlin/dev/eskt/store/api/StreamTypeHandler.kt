package dev.eskt.store.api

public interface StreamTypeHandler<E, I> {
    public val streamType: StreamType<E, I>

    /**
     * Load events from an event stream.
     */
    public fun loadStream(
        streamId: I,
        sinceVersion: Int = 0,
    ): List<EventEnvelope<E, I>>

    /**
     * Append new events into an event stream.
     *
     * @return the version of the aggregate after those events are appended, or;
     * @throws StreamVersionMismatchException when the stream currently is not in the expected version.
     */
    public fun appendStream(
        streamId: I,
        expectedVersion: Int,
        events: List<E>,
        metadata: EventMetadata = emptyMap(),
    ): Int
}

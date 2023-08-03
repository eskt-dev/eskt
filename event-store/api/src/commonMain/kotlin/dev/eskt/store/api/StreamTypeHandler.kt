package dev.eskt.store.api

public interface StreamTypeHandler<I, E> {
    public val streamType: StreamType<I, E>

    public fun loadStream(
        streamId: I,
        sinceVersion: Int = 0,
    ): Result<List<EventEnvelope<I, E>>, LoadFailure>

    /**
     * Append new events into an event stream.
     *
     * @return
     * 1. The version of the aggregate after those events are appended, or;
     * 1. An [AppendFailure] with the specific failure that prevented the new events to be appended.
     */
    public fun appendStream(
        streamId: I,
        expectedVersion: Int,
        events: List<E>,
    ): Result<Int, AppendFailure>
}

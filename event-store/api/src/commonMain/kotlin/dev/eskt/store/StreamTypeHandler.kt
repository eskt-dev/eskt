package dev.eskt.store

interface StreamTypeHandler<I, E> {
    /**
     * Append new events into an event stream.
     *
     * @return
     * 1. The version of the aggregate after those events are appended, or;
     * 1. An [AppendFailure] with the specific failure that prevented the new events to be appended.
     */
    fun appendStream(streamId: I, expectedVersion: Int, events: List<E>): Result<Int, AppendFailure>
}

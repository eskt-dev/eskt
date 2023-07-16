package dev.eskt.store

import dev.eskt.store.storage.api.ExpectedVersionMismatch

public class InMemoryStreamTypeHandler<I, E> internal constructor(
    override val streamType: StreamType<I, E>,
    private val storage: InMemoryStorage,
) : StreamTypeHandler<I, E> {
    override fun loadStream(streamId: I, sinceVersion: Int): Result<List<EventEnvelope<I, E>>, LoadFailure> {
        return Result.Ok(
            storage.instanceEnvelopes(streamId, sinceVersion),
        )
    }

    override fun appendStream(streamId: I, expectedVersion: Int, events: List<E>): Result<Int, AppendFailure> {
        try {
            storage.add(streamType, streamId, expectedVersion, events, emptyMap())
        } catch (e: ExpectedVersionMismatch) {
            return Result.Failure(AppendFailure.ExpectedVersionMismatch(e.currentVersion, e.expectedVersion))
        }
        return Result.Ok(expectedVersion + events.size)
    }
}

package dev.eskt.store

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
        val streamEvents: List<E> = storage.instanceEvents(streamId)
        if (streamEvents.size != expectedVersion) {
            return Result.Failure(AppendFailure.ExpectedVersionMismatch(streamEvents.size, expectedVersion))
        }
        events.forEach {
            storage.add(streamType, streamId, expectedVersion + 1, it)
        }
        val versionAfterAppend = storage.instanceEvents<I, E>(streamId).size
        return Result.Ok(versionAfterAppend)
    }
}

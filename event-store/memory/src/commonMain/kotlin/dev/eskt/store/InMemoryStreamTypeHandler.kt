package dev.eskt.store

class InMemoryStreamTypeHandler<I, E> internal constructor(
    private val storage: InMemoryStorage,
) : StreamTypeHandler<I, E> {
    override fun appendStream(streamId: I, expectedVersion: Int, events: List<E>): Result<Int, AppendFailure> {
        val streamEvents: List<E> = storage.instanceEvents(streamId)
        if (streamEvents.size != expectedVersion) {
            return Result.Failure(AppendFailure.ExpectedVersionMismatch(streamEvents.size, expectedVersion))
        }
        events.forEach {
            storage.add(streamId, it)
        }
        val versionAfterAppend = storage.instanceEvents<I, E>(streamId).size
        return Result.Ok(versionAfterAppend)
    }
}

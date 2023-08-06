package dev.eskt.store.impl.fs

import dev.eskt.store.api.EventStore
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StreamTypeHandler

public class FileSystemEventStore internal constructor(
    config: FileSystemConfig,
    private val storage: FileSystemStorage = FileSystemStorage(config),
) : EventStore {
    public constructor(basePath: String, block: FileSystemConfigBuilder.() -> Unit) : this(
        FileSystemConfigBuilder(basePath)
            .apply(block)
            .build(),
    )

    override fun <I, E> withStreamType(type: StreamType<I, E>): StreamTypeHandler<I, E> {
        return FileSystemStreamTypeHandler(type, storage)
    }
}

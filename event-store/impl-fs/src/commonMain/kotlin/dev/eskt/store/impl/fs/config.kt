package dev.eskt.store.impl.fs

import dev.eskt.store.api.BinarySerializableStreamType
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.Serializer
import dev.eskt.store.api.StreamType
import dev.eskt.store.impl.common.binary.serialization.DefaultEventMetadataSerializer
import okio.Path
import okio.Path.Companion.toPath

internal class FileSystemConfig(
    val basePath: Path,
    val eventMetadataSerializer: Serializer<EventMetadata, ByteArray>,
    val registeredTypes: List<StreamType<*, *>>,
)

public class FileSystemConfigBuilder(
    private val basePath: String,
) {
    private val registeredTypes = mutableListOf<StreamType<*, *>>()
    private var eventMetadataSerializer: Serializer<EventMetadata, ByteArray> = DefaultEventMetadataSerializer

    public fun <E, I, T> registerStreamType(streamType: T) where T : StreamType<E, I>, T : BinarySerializableStreamType<E, I> {
        registeredTypes += streamType as BinarySerializableStreamType<*, *>
    }

    public fun eventMetadataSerializer(eventMetadataSerializer: Serializer<EventMetadata, ByteArray>) {
        this.eventMetadataSerializer = eventMetadataSerializer
    }

    internal fun build(): FileSystemConfig = FileSystemConfig(
        basePath = basePath.toPath(true),
        eventMetadataSerializer = eventMetadataSerializer,
        registeredTypes = registeredTypes,
    )
}

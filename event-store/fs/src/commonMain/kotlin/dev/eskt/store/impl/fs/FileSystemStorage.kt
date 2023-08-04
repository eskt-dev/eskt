package dev.eskt.store.impl.fs

import dev.eskt.store.api.BinarySerializableStreamType
import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.Serializer
import dev.eskt.store.api.StreamType
import dev.eskt.store.impl.common.binary.serialization.DefaultEventMetadataSerializer
import dev.eskt.store.storage.api.ExpectedVersionMismatch
import dev.eskt.store.storage.api.Storage
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import okio.Buffer
import okio.FileHandle
import okio.Path
import okio.buffer
import okio.use
import okio.withLock

@OptIn(ExperimentalSerializationApi::class)
public class FileSystemStorage internal constructor(
    private val basePath: Path,
    internal var eventMetadataSerializer: Serializer<EventMetadata, ByteArray> = DefaultEventMetadataSerializer,
) : Storage {
    public companion object {
        internal val fs = eventStoreFileSystem()
        private val walEntrySerializer = ProtoBuf { }
        private const val streamEntrySizeInBytes: Long = (Long.SIZE_BYTES).toLong()
        private const val positionEntrySizeInBytes: Long = (Long.SIZE_BYTES).toLong()
    }

    private val walPath = basePath / "wal"
    private val posPath = basePath / "pos"

    init {
        // TODO implement wal -> index sync check
    }

    override fun <I, E> add(streamType: StreamType<I, E>, streamId: I, expectedVersion: Int, events: List<E>, metadata: EventMetadata) {
        val binarySerializableStreamType = streamType as BinarySerializableStreamType<I, E>
        val streamTypeFolder = basePath / streamType.id.lowercase()
        fs.createDirectories(streamTypeFolder)

        val streamIdAsString: String = binarySerializableStreamType.idSerializer.serialize(streamId)

        val stream = streamTypeFolder / ("stream-$streamIdAsString")

        // creating payloads in serialized form before acquiring any locks
        val metadataPayload = eventMetadataSerializer.serialize(metadata)
        val walEntries = events.mapIndexed { index, event ->
            val eventPayload = binarySerializableStreamType.binaryEventSerializer.serialize(event)
            val walEntry = WalEntry(
                type = streamType.id,
                id = streamIdAsString,
                version = expectedVersion + index + 1,
                eventPayload = eventPayload,
                metadataPayload = metadataPayload,
            )
            walEntrySerializer.encodeToByteArray(WalEntry.serializer(), walEntry)
        }

        fs.openReadWrite(stream).use { streamHandle ->
            streamHandle.lock.withLock {
                if (streamHandle.size() != expectedVersion * streamEntrySizeInBytes) {
                    throw ExpectedVersionMismatch((streamHandle.size() / streamEntrySizeInBytes).toInt(), expectedVersion)
                }

                val walAddresses = fs.openReadWrite(walPath).use { walHandle ->
                    walHandle.lock.withLock {
                        // calculate position based on previous wal entry
                        val walAddressFirstEvent = walHandle.size()
                        val position = if (walAddressFirstEvent > 0) {
                            val positionLastEntry = walHandle.readLong(walAddressFirstEvent - 8)
                            positionLastEntry + 1
                        } else {
                            1L
                        }

                        val walAddresses = mutableListOf(walAddressFirstEvent)

                        // write wal entry
                        val walBuffer = Buffer()
                        walEntries.forEach { walEntryBytes ->
                            walBuffer.writeInt(walEntryBytes.size)
                            walBuffer.write(walEntryBytes)
                            walBuffer.writeInt(walEntryBytes.size)
                            walBuffer.writeLong(position)
                            walAddresses.add(walAddresses.last() + 4 + walEntryBytes.size + 4 + 8)
                        }
                        walHandle.appendingSink().apply {
                            write(walBuffer, walBuffer.size)
                            flush()
                        }
                        walAddresses.removeLast()

                        // write position index
                        fs.openReadWrite(posPath).use { posHandle ->
                            val posBuffer = Buffer()
                            walAddresses.forEach { walAddress ->
                                posBuffer.writeLong(walAddress)
                            }
                            posHandle.appendingSink().write(posBuffer, posBuffer.size)
                        }

                        walAddresses
                    }
                }
                // write stream index
                val streamBuffer = Buffer()
                walAddresses.forEach { walAddress ->
                    streamBuffer.writeLong(walAddress)
                }
                streamHandle.appendingSink().write(streamBuffer, streamBuffer.size)
            }
        }
    }

    override fun <I, E> add(streamType: StreamType<I, E>, streamId: I, version: Int, event: E, metadata: EventMetadata) {
        add(streamType, streamId, expectedVersion = version - 1, listOf(event), metadata)
    }

    internal fun <I, E> instanceEnvelopes(streamType: StreamType<I, E>, streamId: I, sinceVersion: Int): List<EventEnvelope<I, E>> {
        val binarySerializableStreamType = streamType as BinarySerializableStreamType<I, E>
        val streamTypeFolder = basePath / streamType.id.lowercase()

        val streamIdAsString: String = binarySerializableStreamType.idSerializer.serialize(streamId)

        val stream = streamTypeFolder / ("stream-$streamIdAsString")
        if (!fs.exists(stream)) {
            return emptyList()
        }

        fs.openReadOnly(stream).use { streamHandle ->
            val streamSource = streamHandle.source(sinceVersion * streamEntrySizeInBytes).buffer()
            fs.openReadOnly(walPath).use { walHandle ->
                return buildList {
                    while (!streamSource.exhausted()) {
                        val addr = streamSource.readLong()
                        val envelope = walHandle.readEventEnvelopeAt<I, E>(addr, streamTypeFinder = { _ -> streamType })
                        add(envelope)
                    }
                }
            }
        }
    }

    override fun <I, E> getEvent(streamType: StreamType<I, E>, position: Long): EventEnvelope<I, E> {
        if (!fs.exists(posPath)) {
            throw IllegalStateException("Event store is empty, file $posPath does not exist")
        }

        return fs.openReadOnly(posPath).use { posHandle ->
            val posSource = posHandle.source((position - 1) * positionEntrySizeInBytes).buffer()
            fs.openReadOnly(walPath).use { walHandle ->
                walHandle.readEventEnvelopeAt(posSource.readLong(), streamTypeFinder = { _ -> streamType })
            }
        }
    }

    override fun <I, E> getStreamEvent(streamType: StreamType<I, E>, streamId: I, version: Int): EventEnvelope<I, E> {
        val binarySerializableStreamType = streamType as BinarySerializableStreamType<I, E>
        val streamTypeFolder = basePath / streamType.id.lowercase()

        val streamIdAsString: String = binarySerializableStreamType.idSerializer.serialize(streamId)

        val stream = streamTypeFolder / ("stream-$streamIdAsString")
        if (!fs.exists(stream)) {
            throw IllegalStateException("Stream $streamId is empty, file $stream does not exist")
        }

        return fs.openReadOnly(stream).use { streamHandle ->
            val streamSource = streamHandle.source((version - 1) * streamEntrySizeInBytes).buffer()
            fs.openReadOnly(walPath).use { walHandle ->
                walHandle.readEventEnvelopeAt<I, E>(streamSource.readLong(), streamTypeFinder = { _ -> streamType })
            }
        }
    }

    private fun <I, E> FileHandle.readEventEnvelopeAt(
        addr: Long,
        streamTypeFinder: (typeId: String) -> StreamType<*, *>,
    ): EventEnvelope<I, E> {
        val walBuffer = source(addr).buffer()
        val entrySize = walBuffer.readInt()
        val walEntryByteArray = walBuffer.readByteArray(entrySize.toLong())
        val walEntry = walEntrySerializer.decodeFromByteArray(WalEntry.serializer(), walEntryByteArray)
        walBuffer.readInt() // ignore second copy of the entry size
        val position = walBuffer.readLong()

        val streamType = streamTypeFinder(walEntry.type)

        @Suppress("UNCHECKED_CAST")
        val binarySerializableStreamType = streamType as BinarySerializableStreamType<I, E>

        return EventEnvelope(
            streamType,
            binarySerializableStreamType.idSerializer.deserialize(walEntry.id),
            walEntry.version,
            position,
            eventMetadataSerializer.deserialize(walEntry.metadataPayload),
            binarySerializableStreamType.binaryEventSerializer.deserialize(walEntry.eventPayload),
        )
    }

    @Serializable
    private class WalEntry(
        @ProtoNumber(1)
        val type: String,
        @ProtoNumber(2)
        val id: String,
        @ProtoNumber(3)
        val version: Int,
        @ProtoNumber(4)
        val eventPayload: ByteArray,
        @ProtoNumber(5)
        val metadataPayload: ByteArray,
    )
}

package dev.eskt.store.impl.fs

import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.Serializer
import dev.eskt.store.api.StreamType
import dev.eskt.store.storage.api.Storage
import dev.eskt.store.storage.api.StorageVersionMismatchException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import okio.Buffer
import okio.FileHandle
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import okio.withLock

@OptIn(ExperimentalSerializationApi::class)
public class FileSystemStorage internal constructor(
    private val config: FileSystemConfig,
) : Storage {
    private val basePath = config.basePath
    private val eventMetadataSerializer = config.eventMetadataSerializer
    private val registeredTypes = config.registeredTypes.associateBy { it.id }

    public companion object {
        internal val fs = eventStoreFileSystem()
        private val walEntrySerializer = ProtoBuf { }
        private const val STREAM_ENTRY_SIZE_IN_BYTES: Long = (Long.SIZE_BYTES).toLong()
        private const val POSITION_ENTRY_SIZE_IN_BYTES: Long = (Long.SIZE_BYTES).toLong()
    }

    private val walPath = basePath / "wal"
    private val posPath = basePath / "pos"

    init {
        // TODO implement wal -> index sync check
    }

    override fun <E, I> add(streamType: StreamType<E, I>, streamId: I, expectedVersion: Int, events: List<E>, metadata: EventMetadata) {
        if (streamType.id !in registeredTypes) throwIfNotRegistered(streamType.id)
        val streamPath = basePath / toPathComponent(streamId, streamType.stringIdSerializer)
        fs.createDirectories(streamPath.parent!!)

        // creating payloads in serialized form before acquiring any locks
        val metadataPayload = eventMetadataSerializer.serialize(metadata)
        val walEntries = events.mapIndexed { index, event ->
            val eventPayload = streamType.binaryEventSerializer.serialize(event)
            val walEntry = WalEntry(
                type = streamType.id,
                id = streamType.stringIdSerializer.serialize(streamId),
                version = expectedVersion + index + 1,
                eventPayload = eventPayload,
                metadataPayload = metadataPayload,
            )
            walEntrySerializer.encodeToByteArray(WalEntry.serializer(), walEntry)
        }

        fs.openReadWrite(streamPath).use { streamHandle ->
            streamHandle.lock.withLock {
                if (streamHandle.size() != expectedVersion * STREAM_ENTRY_SIZE_IN_BYTES) {
                    throw StorageVersionMismatchException((streamHandle.size() / STREAM_ENTRY_SIZE_IN_BYTES).toInt(), expectedVersion)
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
                        walEntries.forEachIndexed { index, walEntryBytes ->
                            walBuffer.writeInt(walEntryBytes.size)
                            walBuffer.write(walEntryBytes)
                            walBuffer.writeInt(walEntryBytes.size)
                            walBuffer.writeLong(position + index)
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

    override fun <E, I> getStreamEvents(streamType: StreamType<E, I>, streamId: I, sinceVersion: Int): List<EventEnvelope<E, I>> {
        if (streamType.id !in registeredTypes) throwIfNotRegistered(streamType.id)
        val streamPath = basePath / toPathComponent(streamId, streamType.stringIdSerializer)

        if (!fs.exists(streamPath)) {
            return emptyList()
        }

        fs.openReadOnly(streamPath).use { streamHandle ->
            val streamSource = streamHandle.source(sinceVersion * STREAM_ENTRY_SIZE_IN_BYTES).buffer()
            fs.openReadOnly(walPath).use { walHandle ->
                return buildList {
                    while (!streamSource.exhausted()) {
                        val addr = streamSource.readLong()
                        val envelope = walHandle.readEventEnvelopeAt<E, I>(addr, streamTypeFinder = { id -> registeredTypes[id].asTyped() })
                        add(envelope)
                    }
                }
            }
        }
    }

    public override fun <E, I, R> useStreamEvents(
        streamType: StreamType<E, I>,
        streamId: I,
        sinceVersion: Int,
        consume: (Sequence<EventEnvelope<E, I>>) -> R,
    ): R {
        // TODO optimize this implementation, we don't need to load the entire list
        val events = getStreamEvents(streamType, streamId, sinceVersion)
        return consume.invoke(events.asSequence())
    }

    override fun <E, I> getEventByPosition(position: Long): EventEnvelope<E, I> {
        if (!fs.exists(posPath)) {
            throw IllegalStateException("Event store is empty, file $posPath does not exist")
        }

        return fs.openReadOnly(posPath).use { posHandle ->
            val posSource = posHandle.source((position - 1) * POSITION_ENTRY_SIZE_IN_BYTES).buffer()
            val addr = posSource.readLong()
            fs.openReadOnly(walPath).use { walHandle ->
                walHandle.readEventEnvelopeAt(addr, streamTypeFinder = { id -> registeredTypes[id].asTyped() })
            }
        }
    }

    override fun loadEventBatch(sincePosition: Long, batchSize: Int): List<EventEnvelope<Any, Any>> {
        return loadEventBatchInternal(sincePosition, batchSize, null)
    }

    override fun <E, I> loadEventBatch(sincePosition: Long, batchSize: Int, streamType: StreamType<E, I>): List<EventEnvelope<E, I>> {
        return loadEventBatchInternal(sincePosition, batchSize, streamType)
    }

    private fun <E, I> loadEventBatchInternal(sincePosition: Long, batchSize: Int, streamType: StreamType<E, I>?): List<EventEnvelope<E, I>> {
        if (!fs.exists(posPath)) {
            throw IllegalStateException("Event store is empty, file $posPath does not exist")
        }

        fs.openReadOnly(posPath).use { posHandle ->
            val posSource = posHandle.source(sincePosition * POSITION_ENTRY_SIZE_IN_BYTES).buffer()
            fs.openReadOnly(walPath).use { walHandle ->
                var added = 0
                return buildList {
                    while (!posSource.exhausted()) {
                        val addr = posSource.readLong()
                        val envelope = walHandle.readEventEnvelopeAt<E, I>(addr, streamTypeFinder = { id -> registeredTypes[id].asTyped() })
                        if (streamType == null || streamType == envelope.streamType) {
                            add(envelope)
                            added++
                            if (added == batchSize) break
                        }
                    }
                }
            }
        }
    }

    override fun <E, I> getEventByStreamVersion(streamType: StreamType<E, I>, streamId: I, version: Int): EventEnvelope<E, I> {
        if (streamType.id !in registeredTypes) throwIfNotRegistered(streamType.id)

        val streamPath = basePath / toPathComponent(streamId, streamType.stringIdSerializer)

        if (!fs.exists(streamPath)) {
            throw IllegalStateException("Stream $streamId is empty, file $streamPath does not exist")
        }

        return fs.openReadOnly(streamPath).use { streamHandle ->
            val streamSource = streamHandle.source((version - 1) * STREAM_ENTRY_SIZE_IN_BYTES).buffer()
            val addr = streamSource.readLong()
            fs.openReadOnly(walPath).use { walHandle ->
                walHandle.readEventEnvelopeAt(addr, streamTypeFinder = { id -> registeredTypes[id].asTyped() })
            }
        }
    }

    private fun throwIfNotRegistered(streamTypeId: String): Nothing {
        throw IllegalStateException("Unregistered type: $streamTypeId")
    }

    private fun <E, I> FileHandle.readEventEnvelopeAt(
        addr: Long,
        streamTypeFinder: (typeId: String) -> StreamType<E, I>,
    ): EventEnvelope<E, I> {
        val walBuffer = source(addr).buffer()
        val entrySize = walBuffer.readInt()
        val walEntryByteArray = walBuffer.readByteArray(entrySize.toLong())
        val walEntry = walEntrySerializer.decodeFromByteArray(WalEntry.serializer(), walEntryByteArray)
        walBuffer.readInt() // ignore second copy of the entry size
        val position = walBuffer.readLong()

        val streamType = streamTypeFinder(walEntry.type)

        return EventEnvelope(
            streamType,
            streamType.stringIdSerializer.deserialize(walEntry.id),
            walEntry.version,
            position,
            eventMetadataSerializer.deserialize(walEntry.metadataPayload),
            streamType.binaryEventSerializer.deserialize(walEntry.eventPayload),
        )
    }

    private fun <I> toPathComponent(streamId: I, serializer: Serializer<I, String>): Path {
        return serializer.serialize(streamId).split('/').fold("streams".toPath()) { current, next -> current / next }
    }

    @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
    private inline fun <E, I> StreamType<*, *>?.asTyped(): StreamType<E, I> {
        return this as? StreamType<E, I> ?: throwIfNotRegistered(this?.id ?: "")
    }

    @Suppress("UNCHECKED_CAST")
    private val <E, I> StreamType<E, I>.stringIdSerializer: Serializer<I, String>
        get() = config.idSerializers[this] as Serializer<I, String>

    @Suppress("UNCHECKED_CAST")
    private val <E, I> StreamType<E, I>.binaryEventSerializer: Serializer<E, ByteArray>
        get() = config.payloadSerializers[this] as Serializer<E, ByteArray>

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

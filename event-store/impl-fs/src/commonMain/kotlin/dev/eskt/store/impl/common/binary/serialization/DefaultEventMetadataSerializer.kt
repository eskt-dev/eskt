package dev.eskt.store.impl.common.binary.serialization

import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.modules.SerializersModule

/**
 * Default binary serializer for [EventMetadata].
 */
// move this to a `impl-common-binary` module when there is another binary implementation
@OptIn(ExperimentalSerializationApi::class)
public object DefaultEventMetadataSerializer : Serializer<EventMetadata, ByteArray> {
    private val cbor = Cbor {
        serializersModule = SerializersModule {
            contextual(Any::class, DefaultEventMetadataEntryValueKSerializer)
        }
    }

    override fun serialize(obj: EventMetadata): ByteArray {
        return cbor.encodeToByteArray(obj)
    }

    override fun deserialize(payload: ByteArray): EventMetadata {
        return cbor.decodeFromByteArray(payload)
    }
}

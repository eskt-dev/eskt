package dev.eskt.store.impl.common.string.serialization

import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

// TODO move this to a `impl-common-string` module when there is another binary implementation

/**
 * Default string serializer for [EventMetadata].
 */
public object DefaultEventMetadataSerializer : Serializer<EventMetadata, String> {
    private val json = Json {
        serializersModule = SerializersModule {
            contextual(Any::class, DefaultEventMetadataEntryValueKSerializer)
        }
    }

    private val serializer = json.serializersModule.serializer<EventMetadata>()

    override fun serialize(obj: EventMetadata): String {
        return json.encodeToString(serializer, obj)
    }

    override fun deserialize(payload: String): EventMetadata {
        return json.decodeFromString(serializer, payload)
    }
}

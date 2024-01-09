package dev.eskt.store.test.w.driver

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import dev.eskt.store.api.BinarySerializableStreamType
import dev.eskt.store.api.Serializer
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StringSerializableStreamType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
public data object DriverStreamType :
    StreamType<DriverEvent, Uuid>,
    BinarySerializableStreamType<DriverEvent, Uuid>,
    StringSerializableStreamType<DriverEvent, Uuid> {
    private val eventSerializer = DriverEvent.serializer()

    override val id: String = "Driver"

    override val stringIdSerializer: Serializer<Uuid, String> = object : Serializer<Uuid, String> {
        override fun serialize(obj: Uuid): String {
            return obj.toString()
        }

        override fun deserialize(payload: String): Uuid {
            return uuidFrom(payload)
        }
    }

    override val stringEventSerializer: Serializer<DriverEvent, String> = object : Serializer<DriverEvent, String> {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        override fun serialize(obj: DriverEvent): String {
            return json.encodeToString(eventSerializer, obj)
        }

        override fun deserialize(payload: String): DriverEvent {
            return json.decodeFromString(eventSerializer, payload)
        }
    }

    override val binaryEventSerializer: Serializer<DriverEvent, ByteArray> = object : Serializer<DriverEvent, ByteArray> {
        private val cbor = Cbor {
            ignoreUnknownKeys = true
        }

        override fun serialize(obj: DriverEvent): ByteArray {
            return cbor.encodeToByteArray(eventSerializer, obj)
        }

        override fun deserialize(payload: ByteArray): DriverEvent {
            return cbor.decodeFromByteArray(eventSerializer, payload)
        }
    }
}

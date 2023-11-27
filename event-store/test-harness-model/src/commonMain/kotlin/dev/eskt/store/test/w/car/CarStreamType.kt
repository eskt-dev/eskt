package dev.eskt.store.test.w.car

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
public data object CarStreamType :
    StreamType<Uuid, CarEvent>,
    BinarySerializableStreamType<Uuid, CarEvent>,
    StringSerializableStreamType<Uuid, CarEvent> {

    private val eventSerializer = CarEvent.serializer()

    override val id: String = "Car"

    override val stringIdSerializer: Serializer<Uuid, String> = object : Serializer<Uuid, String> {
        override fun serialize(obj: Uuid): String {
            return obj.toString()
        }

        override fun deserialize(payload: String): Uuid {
            return uuidFrom(payload)
        }
    }

    override val stringEventSerializer: Serializer<CarEvent, String> = object : Serializer<CarEvent, String> {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        override fun serialize(obj: CarEvent): String {
            return json.encodeToString(eventSerializer, obj)
        }

        override fun deserialize(payload: String): CarEvent {
            return json.decodeFromString(eventSerializer, payload)
        }
    }

    override val binaryEventSerializer: Serializer<CarEvent, ByteArray> = object : Serializer<CarEvent, ByteArray> {
        private val cbor = Cbor {
            ignoreUnknownKeys = true
        }

        override fun serialize(obj: CarEvent): ByteArray {
            return cbor.encodeToByteArray(eventSerializer, obj)
        }

        override fun deserialize(payload: ByteArray): CarEvent {
            return cbor.decodeFromByteArray(eventSerializer, payload)
        }
    }
}

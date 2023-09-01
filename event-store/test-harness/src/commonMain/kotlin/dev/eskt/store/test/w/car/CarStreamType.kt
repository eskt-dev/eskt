package dev.eskt.store.test.w.car

import dev.eskt.store.api.BinarySerializableStreamType
import dev.eskt.store.api.DatabaseStreamType
import dev.eskt.store.api.Serializer
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StringSerializableStreamType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
public object CarStreamType :
    StreamType<String, CarEvent>,
    BinarySerializableStreamType<String, CarEvent>,
    StringSerializableStreamType<String, CarEvent>,
    DatabaseStreamType<String, CarEvent> {
    private val eventSerializer = CarEvent.serializer()

    override val eventTableSchema: String = "event_store"
    override val eventTableName: String = "event_1"

    override val id: String = "Car"

    override val stringIdSerializer: Serializer<String, String> = object : Serializer<String, String> {
        override fun serialize(obj: String): String {
            return obj
        }

        override fun deserialize(payload: String): String {
            return payload
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

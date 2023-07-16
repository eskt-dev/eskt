package dev.eskt.store.test.w.car

import dev.eskt.store.BinarySerializableStreamType
import dev.eskt.store.Serializer
import dev.eskt.store.StreamType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor

@OptIn(ExperimentalSerializationApi::class)
public object CarStreamType : StreamType<String, CarEvent>, BinarySerializableStreamType<String, CarEvent> {
    private val serializer = CarEvent.serializer()

    override val id: String = "Car"

    override val idSerializer: Serializer<String, String> = object : Serializer<String, String> {
        override fun serialize(obj: String): String {
            return obj
        }

        override fun deserialize(payload: String): String {
            return payload
        }
    }

    override val binaryEventSerializer: Serializer<CarEvent, ByteArray> = object : Serializer<CarEvent, ByteArray> {
        private val cbor = Cbor {
            ignoreUnknownKeys = true
        }

        override fun serialize(obj: CarEvent): ByteArray {
            return cbor.encodeToByteArray(serializer, obj)
        }

        override fun deserialize(payload: ByteArray): CarEvent {
            return cbor.decodeFromByteArray(serializer, payload)
        }
    }
}

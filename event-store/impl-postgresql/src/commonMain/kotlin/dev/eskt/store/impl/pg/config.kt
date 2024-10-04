package dev.eskt.store.impl.pg

import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.Serializer
import dev.eskt.store.api.StreamType
import dev.eskt.store.impl.common.string.serialization.DefaultEventMetadataSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.util.UUID
import kotlin.reflect.KClass

internal class PostgresqlConfig(
    val registeredTypes: List<StreamType<*, *>>,
    internal val payloadSerializers: Map<StreamType<*, *>, Serializer<*, String>>,
    internal val idSerializers: Map<StreamType<*, *>, Serializer<*, String>>,
    internal val eventMetadataSerializer: Serializer<EventMetadata, String>,
    val dataSource: DataSource,
    val eventTable: String,
    val eventWriteTable: String = eventTable,
) {
    private val registeredTypeById: Map<String, StreamType<*, *>> by lazy { registeredTypes.associateBy { it.id } }

    @Suppress("UNCHECKED_CAST")
    fun <E, I, S : StreamType<E, I>> streamType(id: String): S {
        return registeredTypeById[id] as S? ?: throw IllegalStateException("Invalid stream type id $id")
    }

    val tableInfo = TableInfo(
        table = eventTable,
        writeTable = eventWriteTable,
    )
}

internal data class ConnectionConfig(
    val host: String,
    val port: Int,
    val database: String,
    val schema: String,
    val user: String,
    val pass: String,
    val minPoolSize: Int = 1,
    val maxPoolSize: Int = 2,
)

internal data class TableInfo(
    val table: String,
    val writeTable: String,
)

public class PostgresqlConfigBuilder(
    private val datasource: DataSource,
    private val eventTable: String,
    /**
     * In case you need your event table to be a view, you can define a different table name that will be used for write operations only.
     */
    private val eventWriteTable: String = eventTable,
) {
    private val registeredTypes = mutableListOf<StreamType<*, *>>()
    private val payloadSerializers = mutableMapOf<StreamType<*, *>, Serializer<*, String>>()
    private val idSerializers = mutableMapOf<StreamType<*, *>, Serializer<*, String>>()
    private var eventMetadataSerializer: Serializer<EventMetadata, String> = DefaultEventMetadataSerializer

    public fun <E, I, T> registerStreamTypeWith(streamType: T, payloadSerializer: Serializer<E, String>, idSerializer: Serializer<I, String>)
    where T : StreamType<E, I> {
        registeredTypes += streamType as StreamType<*, *>
        payloadSerializers[streamType] = payloadSerializer as Serializer<*, String>
        idSerializers[streamType] = idSerializer as Serializer<*, String>
    }

    public inline fun <reified E : Any, reified I : Any, T> registerStreamType(
        streamType: T,
        payloadSerializer: Serializer<E, String> = createDefaultPayloadSerializer(E::class),
        idSerializer: Serializer<I, String> = createDefaultIdSerializer(I::class),
    )
    where T : StreamType<E, I> {
        registerStreamTypeWith(streamType, payloadSerializer, idSerializer)
    }

    public fun eventMetadataSerializer(eventMetadataSerializer: Serializer<EventMetadata, String>) {
        this.eventMetadataSerializer = eventMetadataSerializer
    }

    @OptIn(InternalSerializationApi::class)
    public fun <E : Any> createDefaultPayloadSerializer(type: KClass<E>): Serializer<E, String> {
        return object : Serializer<E, String> {
            val json = Json
            val serializer = try {
                type.serializer()
            } catch (e: kotlinx.serialization.SerializationException) {
                throw IllegalStateException("$type is not marked with @Serializable, please register this type with an explicit serializer", e)
            }

            override fun serialize(obj: E): String {
                return json.encodeToString(serializer, obj)
            }

            override fun deserialize(payload: String): E {
                return json.decodeFromString(serializer, payload)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    public fun <I : Any> createDefaultIdSerializer(type: KClass<I>): Serializer<I, String> {
        return try {
            val serializer = type.serializer()
            object : Serializer<I, String> {
                val json = Json
                override fun serialize(obj: I): String {
                    return json.encodeToString(serializer, obj)
                }

                override fun deserialize(payload: String): I {
                    return json.decodeFromString(serializer, payload)
                }
            }
        } catch (e: kotlinx.serialization.SerializationException) {
            when (type) {
                String::class -> object : Serializer<I, String> {
                    override fun serialize(obj: I): String = obj as String
                    override fun deserialize(payload: String): I = payload as I
                }
                UUID::class -> object : Serializer<I, String> {
                    override fun serialize(obj: I): String = obj.toString()
                    override fun deserialize(payload: String): I = UUID.fromString(payload) as I
                }
                else -> throw IllegalStateException(
                    "$type is not marked with @Serializable and cannot be serialized automatically, please register this type with an explicit id serializer",
                )
            }
        }
    }

    internal fun build(): PostgresqlConfig = PostgresqlConfig(
        registeredTypes = registeredTypes,
        payloadSerializers = payloadSerializers,
        idSerializers = idSerializers,
        eventMetadataSerializer = eventMetadataSerializer,
        dataSource = datasource,
        eventTable = eventTable,
        eventWriteTable = eventWriteTable,
    )
}

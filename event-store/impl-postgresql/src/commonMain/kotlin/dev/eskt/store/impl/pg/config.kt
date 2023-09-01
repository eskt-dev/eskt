package dev.eskt.store.impl.pg

import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.Serializer
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StringSerializableStreamType
import dev.eskt.store.impl.common.string.serialization.DefaultEventMetadataSerializer

internal class PostgresqlConfig(
    val registeredTypes: List<StreamType<*, *>>,
    val eventMetadataSerializer: Serializer<EventMetadata, String>,
    val connectionConfig: ConnectionConfig,
    val eventTable: String,
) {
    private val registeredTypeById: Map<String, StreamType<*, *>> by lazy { registeredTypes.associateBy { it.id } }

    @Suppress("UNCHECKED_CAST")
    fun <I, E, S : StreamType<I, E>> streamType(id: String): S {
        return registeredTypeById[id] as S? ?: throw IllegalStateException("Invalid stream type id $id")
    }

    val tableInfo = TableInfo(
        table = eventTable,
        payloadType = TableInfo.PayloadType.Json,
    )
}

public data class ConnectionConfig(
    val host: String,
    val port: Int,
    val database: String,
    val schema: String,
    val user: String,
    val pass: String,
    val minPoolSize: Int = 1,
    val maxPoolSize: Int = 10,
)

internal data class TableInfo(
    val table: String,
    val payloadType: PayloadType,
) {
    enum class PayloadType {
        Json,
    }
}

public class PostgresqlConfigBuilder(
    private val connectionConfig: ConnectionConfig,
    private val eventTable: String,
) {
    private val registeredTypes = mutableListOf<StringSerializableStreamType<*, *>>()
    private var eventMetadataSerializer: Serializer<EventMetadata, String> = DefaultEventMetadataSerializer

    public fun <I, E, T> registerStreamType(streamType: T)
    where T : StreamType<I, E>, T : StringSerializableStreamType<I, E> {
        registeredTypes += streamType as StringSerializableStreamType<*, *>
    }

    public fun eventMetadataSerializer(eventMetadataSerializer: Serializer<EventMetadata, String>) {
        this.eventMetadataSerializer = eventMetadataSerializer
    }

    internal fun build(): PostgresqlConfig = PostgresqlConfig(
        registeredTypes = registeredTypes,
        eventMetadataSerializer = eventMetadataSerializer,
        connectionConfig = connectionConfig,
        eventTable = eventTable,
    )
}

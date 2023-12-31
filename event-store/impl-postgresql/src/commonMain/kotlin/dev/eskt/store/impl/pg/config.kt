package dev.eskt.store.impl.pg

import dev.eskt.store.api.EventMetadata
import dev.eskt.store.api.Serializer
import dev.eskt.store.api.StreamType
import dev.eskt.store.api.StringSerializableStreamType
import dev.eskt.store.impl.common.string.serialization.DefaultEventMetadataSerializer

internal class PostgresqlConfig(
    val registeredTypes: List<StreamType<*, *>>,
    val eventMetadataSerializer: Serializer<EventMetadata, String>,
    val dataSource: DataSource,
    val eventTable: String,
) {
    private val registeredTypeById: Map<String, StreamType<*, *>> by lazy { registeredTypes.associateBy { it.id } }

    @Suppress("UNCHECKED_CAST")
    fun <E, I, S : StreamType<E, I>> streamType(id: String): S {
        return registeredTypeById[id] as S? ?: throw IllegalStateException("Invalid stream type id $id")
    }

    val tableInfo = TableInfo(
        table = eventTable,
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
)

public class PostgresqlConfigBuilder(
    private val datasource: DataSource,
    private val eventTable: String,
) {
    private val registeredTypes = mutableListOf<StreamType<*, *>>()
    private var eventMetadataSerializer: Serializer<EventMetadata, String> = DefaultEventMetadataSerializer

    public fun <E, I, T> registerStreamType(streamType: T)
    where T : StreamType<E, I>, T : StringSerializableStreamType<E, I> {
        registeredTypes += streamType as StringSerializableStreamType<*, *>
    }

    public fun eventMetadataSerializer(eventMetadataSerializer: Serializer<EventMetadata, String>) {
        this.eventMetadataSerializer = eventMetadataSerializer
    }

    internal fun build(): PostgresqlConfig = PostgresqlConfig(
        registeredTypes = registeredTypes,
        eventMetadataSerializer = eventMetadataSerializer,
        dataSource = datasource,
        eventTable = eventTable,
    )
}

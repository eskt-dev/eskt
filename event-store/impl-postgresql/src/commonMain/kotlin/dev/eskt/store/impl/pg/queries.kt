package dev.eskt.store.impl.pg

internal fun selectEventByPositionSql(schema: String, eventTable: String) = """
    select position, stream_type, stream_id, version, payload, metadata from $schema.$eventTable
    where position = ?;
    """.trimIndent()

internal fun selectMaxVersionByStreamIdSql(schema: String, eventTable: String) = """
    select max(version) from $schema.$eventTable
    where stream_id = ?;
    """.trimIndent()

internal fun selectEventByStreamIdAndVersionSql(schema: String, eventTable: String) = """
    select position, stream_type, stream_id, version, payload, metadata from $schema.$eventTable
    where stream_id = ? and version > ?
    order by version asc
    limit ?;
    """.trimIndent()

internal fun insertEventSql(schema: String, eventTable: String, columnType: String) = """
    insert into $schema.$eventTable (stream_type, stream_id, version, payload, metadata)
    values (?, ?, ?, ?::$columnType, ?::$columnType);    
    """.trimIndent()

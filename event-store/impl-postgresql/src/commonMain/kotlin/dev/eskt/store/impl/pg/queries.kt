package dev.eskt.store.impl.pg

internal fun selectEventByPositionSql(eventTable: String) = """
    select position, stream_type, stream_id, version, payload, metadata from $eventTable
    where position = ?;
    """.trimIndent()

internal fun selectMaxVersionByStreamIdSql(eventTable: String) = """
    select max(version) from $eventTable
    where stream_id = ?;
    """.trimIndent()

internal fun selectEventByStreamIdAndVersionSql(eventTable: String) = """
    select position, stream_type, stream_id, version, payload, metadata from $eventTable
    where stream_id = ? and version > ?
    order by version asc
    limit ?;
    """.trimIndent()

internal fun insertEventSql(eventTable: String, columnType: String) = """
    insert into $eventTable (stream_type, stream_id, version, payload, metadata)
    values (?, ?, ?, ?::$columnType, ?::$columnType);    
    """.trimIndent()

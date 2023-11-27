package dev.eskt.store.impl.pg

internal fun selectEventByPositionSql(eventTable: String) = """
    select position, stream_type, stream_id, version, payload, metadata 
    from $eventTable
    where position = ?;
    """.trimIndent()

internal fun selectEventSincePositionSql(eventTable: String) = """
    select position, stream_type, stream_id, version, payload, metadata 
    from $eventTable
    where position > ?
    order by position asc
    limit ?;
    """.trimIndent()

internal fun selectEventByTypeSincePositionSql(eventTable: String) = """
    select position, stream_type, stream_id, version, payload, metadata 
    from $eventTable
    where stream_type = ? and position > ?
    order by position asc
    limit ?;
    """.trimIndent()

internal fun selectMaxVersionByStreamIdSql(eventTable: String) = """
    select max(version) 
    from $eventTable
    where stream_id = ?;
    """.trimIndent()

internal fun selectEventByStreamIdAndVersionSql(eventTable: String) = """
    select position, stream_type, stream_id, version, payload, metadata 
    from $eventTable
    where stream_id = ? and version > ?
    order by version asc
    limit ?;
    """.trimIndent()

internal fun insertEventSql(eventTable: String) = """
    insert into $eventTable (stream_type, stream_id, version, payload, metadata)
    values (?, ?, ?, ?, ?);    
    """.trimIndent()

package dev.eskt.store.impl.pg

internal expect class DatabaseAdapter(
    connectionConfig: ConnectionConfig,
) {
    fun getEntryByPosition(
        position: Long,
        tableInfo: TableInfo,
    ): PostgresqlStorage.DatabaseEntry

    fun getEntriesByStreamIdAndVersion(
        streamId: String,
        sinceVersion: Int,
        limit: Int = Int.MAX_VALUE,
        tableInfo: TableInfo,
    ): List<PostgresqlStorage.DatabaseEntry>

    fun persistEntries(
        entries: List<PostgresqlStorage.DatabaseEntry>,
        tableInfo: TableInfo,
    )

    fun close()
}

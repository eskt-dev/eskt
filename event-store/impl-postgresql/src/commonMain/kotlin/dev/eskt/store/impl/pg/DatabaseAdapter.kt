package dev.eskt.store.impl.pg

internal expect class DatabaseAdapter(
    connectionConfig: ConnectionConfig,
) {
    fun getEntryByPosition(
        position: Long,
        tableInfos: List<StreamTypeTableInfo>,
    ): PostgresqlStorage.DatabaseEntry

    fun getEntriesByStreamIdAndVersion(
        streamId: String,
        sinceVersion: Int,
        limit: Int = Int.MAX_VALUE,
        tableInfos: List<StreamTypeTableInfo>,
    ): List<PostgresqlStorage.DatabaseEntry>

    fun persistEntries(
        entries: List<PostgresqlStorage.DatabaseEntry>,
        tableInfo: StreamTypeTableInfo,
    )

    fun close()
}

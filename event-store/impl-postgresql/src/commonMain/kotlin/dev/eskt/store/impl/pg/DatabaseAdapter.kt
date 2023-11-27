package dev.eskt.store.impl.pg

internal expect class DatabaseAdapter(dataSource: DataSource) {
    fun getEntryByPosition(
        position: Long,
        tableInfo: TableInfo,
    ): PostgresqlStorage.DatabaseEntry

    fun getEntryBatch(
        sincePosition: Long,
        batchSize: Int,
        tableInfo: TableInfo,
    ): List<PostgresqlStorage.DatabaseEntry>

    fun getEntryBatch(
        sincePosition: Long,
        batchSize: Int,
        type: String,
        tableInfo: TableInfo,
    ): List<PostgresqlStorage.DatabaseEntry>

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
}

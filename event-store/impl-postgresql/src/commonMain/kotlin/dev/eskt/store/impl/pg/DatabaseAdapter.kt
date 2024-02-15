package dev.eskt.store.impl.pg

import dev.eskt.store.impl.pg.PostgresqlStorage.DatabaseEntry

internal expect class DatabaseAdapter(dataSource: DataSource) {
    fun getEntryByPosition(
        position: Long,
        tableInfo: TableInfo,
    ): DatabaseEntry

    fun getEntryBatch(
        sincePosition: Long,
        batchSize: Int,
        tableInfo: TableInfo,
    ): List<DatabaseEntry>

    fun getEntryBatch(
        sincePosition: Long,
        batchSize: Int,
        type: String,
        tableInfo: TableInfo,
    ): List<DatabaseEntry>

    fun getEntriesByStreamIdAndVersion(
        streamId: String,
        sinceVersion: Int,
        limit: Int = Int.MAX_VALUE,
        tableInfo: TableInfo,
    ): List<DatabaseEntry>

    fun persistEntries(
        streamId: String,
        expectedVersion: Int,
        entries: List<DatabaseEntry>,
        tableInfo: TableInfo,
    )
}

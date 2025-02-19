package dev.eskt.store.impl.pg

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

    fun <R> useEntriesByStreamIdAndVersion(
        streamId: String,
        sinceVersion: Int,
        limit: Int = Int.MAX_VALUE,
        tableInfo: TableInfo,
        consume: (Sequence<DatabaseEntry>) -> R,
    ): R

    fun persistEntries(
        streamId: String,
        expectedVersion: Int,
        entries: List<DatabaseEntry>,
        tableInfo: TableInfo,
    )
}

package dev.eskt.store.impl.pg

internal actual class DatabaseAdapter actual constructor(dataSource: DataSource) {
    actual fun getEntryByPosition(
        position: Long,
        tableInfo: TableInfo,
    ): PostgresqlStorage.DatabaseEntry {
        TODO("Not yet implemented")
    }

    actual fun getEntriesByStreamIdAndVersion(
        streamId: String,
        sinceVersion: Int,
        limit: Int,
        tableInfo: TableInfo,
    ): List<PostgresqlStorage.DatabaseEntry> {
        TODO("Not yet implemented")
    }

    actual fun persistEntries(
        entries: List<PostgresqlStorage.DatabaseEntry>,
        tableInfo: TableInfo,
    ) {
        TODO("Not yet implemented")
    }
}

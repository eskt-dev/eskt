package dev.eskt.store.impl.pg

import dev.eskt.store.storage.api.ExpectedVersionMismatch
import org.postgresql.util.PSQLState
import java.sql.ResultSet
import javax.sql.DataSource

internal actual class DatabaseAdapter actual constructor(
    private val dataSource: DataSource,
) {
    actual fun getEntryByPosition(
        position: Long,
        tableInfo: TableInfo,
    ): PostgresqlStorage.DatabaseEntry {
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectEventByPositionSql(tableInfo.table))
                .use { ps ->
                    ps.setLong(1, position)
                    ps.executeQuery().use { rs ->
                        rs.next()
                        return rs.databaseEntry()
                    }
                }
        }
    }

    actual fun getEntriesByStreamIdAndVersion(
        streamId: String,
        sinceVersion: Int,
        limit: Int,
        tableInfo: TableInfo,
    ): List<PostgresqlStorage.DatabaseEntry> {
        dataSource.connection.use { connection ->
            val stm = selectEventByStreamIdAndVersionSql(tableInfo.table)
            connection.prepareStatement(stm)
                .use { ps ->
                    ps.setString(1, streamId)
                    ps.setInt(2, sinceVersion)
                    ps.setInt(3, limit)
                    ps.executeQuery().use { rs ->
                        return buildList {
                            while (rs.next()) {
                                add(rs.databaseEntry())
                            }
                        }
                    }
                }
        }
    }

    private fun ResultSet.databaseEntry(): PostgresqlStorage.DatabaseEntry {
        return PostgresqlStorage.DatabaseEntry(
            position = getLong("position"),
            type = getString("stream_type"),
            id = getString("stream_id"),
            version = getInt("version"),
            eventPayload = getString("payload"),
            metadataPayload = getString("metadata"),
        )
    }

    actual fun persistEntries(entries: List<PostgresqlStorage.DatabaseEntry>, tableInfo: TableInfo) {
        val expectedVersion = entries[0].version - 1
        dataSource.connection.use { connection ->
            val columnType = when (tableInfo.payloadType) {
                TableInfo.PayloadType.Json -> "json"
            }

            connection.prepareStatement(selectMaxVersionByStreamIdSql(tableInfo.table))
                .use { ps ->
                    ps.setString(1, entries[0].id)
                    ps.executeQuery().use { rs ->
                        rs.next()
                        val currentVersion = rs.getInt(1)
                        if (expectedVersion != currentVersion) {
                            throw ExpectedVersionMismatch(
                                currentVersion,
                                expectedVersion,
                            )
                        }
                    }
                }

            connection.prepareStatement(insertEventSql(tableInfo.table, columnType)).use { ps ->
                entries.forEach { entry ->
                    ps.setString(1, entry.type)
                    ps.setString(2, entry.id)
                    ps.setInt(3, entry.version)
                    ps.setString(4, entry.eventPayload)
                    ps.setString(5, entry.metadataPayload)
                    ps.addBatch()
                }
                try {
                    ps.executeBatch()
                } catch (e: java.sql.BatchUpdateException) {
                    when (val cause = e.cause) {
                        is org.postgresql.util.PSQLException -> {
                            if (cause.sqlState == PSQLState.UNIQUE_VIOLATION.state) {
                                cause.serverErrorMessage
                                    ?.let {
                                        throw ExpectedVersionMismatch(
                                            // if expectedVersion already exists, assuming it's at least 1 version ahead
                                            expectedVersion + 1,
                                            expectedVersion,
                                        )
                                    }
                                    ?: throw e
                            } else {
                                throw e
                            }
                        }

                        else -> throw e
                    }
                }
            }
        }
    }
}

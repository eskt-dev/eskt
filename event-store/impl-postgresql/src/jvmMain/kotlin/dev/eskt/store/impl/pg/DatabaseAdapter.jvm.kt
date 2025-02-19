package dev.eskt.store.impl.pg

import dev.eskt.store.storage.api.StorageVersionMismatchException
import org.postgresql.util.PSQLState
import java.sql.ResultSet
import javax.sql.DataSource

internal actual class DatabaseAdapter actual constructor(
    private val dataSource: DataSource,
) {
    actual fun getEntryByPosition(
        position: Long,
        tableInfo: TableInfo,
    ): DatabaseEntry {
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectEventByPositionSql(tableInfo))
                .use { ps ->
                    ps.setLong(1, position)
                    ps.executeQuery().use { rs ->
                        rs.next()
                        return rs.databaseEntry()
                    }
                }
        }
    }

    actual fun getEntryBatch(
        sincePosition: Long,
        batchSize: Int,
        tableInfo: TableInfo,
    ): List<DatabaseEntry> {
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectEventSincePositionSql(tableInfo))
                .use { ps ->
                    ps.setLong(1, sincePosition)
                    ps.setInt(2, batchSize)
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

    actual fun getEntryBatch(
        sincePosition: Long,
        batchSize: Int,
        type: String,
        tableInfo: TableInfo,
    ): List<DatabaseEntry> {
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectEventByTypeSincePositionSql(tableInfo))
                .use { ps ->
                    ps.setObject(1, type, java.sql.Types.OTHER)
                    ps.setLong(2, sincePosition)
                    ps.setInt(3, batchSize)
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

    actual fun <R> useEntriesByStreamIdAndVersion(
        streamId: String,
        sinceVersion: Int,
        limit: Int,
        tableInfo: TableInfo,
        consume: (Sequence<DatabaseEntry>) -> R,
    ): R {
        dataSource.connection.use { connection ->
            val stm = selectEventByStreamIdAndVersionSql(tableInfo)
            connection.prepareStatement(stm).use { ps ->
                ps.setObject(1, streamId, java.sql.Types.OTHER)
                ps.setInt(2, sinceVersion)
                ps.setInt(3, limit)
                ps.executeQuery().use { rs ->
                    return consume(
                        sequence {
                            while (rs.next()) {
                                yield(rs.databaseEntry())
                            }
                        },
                    )
                }
            }
        }
    }

    private fun ResultSet.databaseEntry(): DatabaseEntry {
        return DatabaseEntry(
            position = getLong("position"),
            type = getString("stream_type"),
            id = getString("stream_id"),
            version = getInt("version"),
            eventPayload = getString("payload"),
            metadataPayload = getString("metadata"),
        )
    }

    actual fun persistEntries(streamId: String, expectedVersion: Int, entries: List<DatabaseEntry>, tableInfo: TableInfo) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectMaxVersionByStreamIdSql(tableInfo)).use { ps ->
                ps.setObject(1, streamId, java.sql.Types.OTHER)
                ps.executeQuery().use { rs ->
                    rs.next()
                    val currentVersion = rs.getInt(1)
                    if (expectedVersion != currentVersion) {
                        throw StorageVersionMismatchException(
                            currentVersion,
                            expectedVersion,
                        )
                    }
                }
            }

            if (entries.isEmpty()) return@use

            connection.prepareStatement(insertEventSql(tableInfo)).use { ps ->
                entries.forEach { entry ->
                    ps.setObject(1, entry.type, java.sql.Types.OTHER)
                    ps.setObject(2, entry.id, java.sql.Types.OTHER)
                    ps.setInt(3, entry.version)
                    ps.setObject(4, entry.eventPayload, java.sql.Types.OTHER)
                    ps.setObject(5, entry.metadataPayload, java.sql.Types.OTHER)
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
                                        throw StorageVersionMismatchException(
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

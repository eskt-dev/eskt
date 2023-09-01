package dev.eskt.store.impl.pg

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.eskt.store.storage.api.ExpectedVersionMismatch
import org.postgresql.util.PSQLState
import java.sql.ResultSet

internal actual class DatabaseAdapter actual constructor(
    connectionConfig: ConnectionConfig,
) {
    // TODO assess an alternative solution where the datasource is provided, so we don't have to manage stateful objects
    private val hikariConfig: HikariConfig = connectionConfig.toHikariConfig()
    private val dataSource: HikariDataSource by lazy {
        HikariDataSource(hikariConfig)
    }

    actual fun getEntryByPosition(
        position: Long,
        tableInfos: List<StreamTypeTableInfo>,
    ): PostgresqlStorage.DatabaseEntry {
        dataSource.connection.use { connection ->
            val tableInfo = tableInfos.single()
            connection.prepareStatement(selectEventByPositionSql(tableInfo.schema, tableInfo.table))
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
        tableInfos: List<StreamTypeTableInfo>,
    ): List<PostgresqlStorage.DatabaseEntry> {
        dataSource.connection.use { connection ->
            val tableInfo = tableInfos.single()
            val stm = selectEventByStreamIdAndVersionSql(tableInfo.schema, tableInfo.table)
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

    actual fun persistEntries(entries: List<PostgresqlStorage.DatabaseEntry>, tableInfo: StreamTypeTableInfo) {
        val expectedVersion = entries[0].version - 1
        dataSource.connection.use { connection ->
            val columnType = when (tableInfo.payloadType) {
                StreamTypeTableInfo.PayloadType.Json -> "json"
            }

            connection.prepareStatement(selectMaxVersionByStreamIdSql(tableInfo.schema, tableInfo.table))
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

            connection.prepareStatement(insertEventSql(tableInfo.schema, tableInfo.table, columnType)).use { ps ->
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

    actual fun close() {
        dataSource.close()
    }
}

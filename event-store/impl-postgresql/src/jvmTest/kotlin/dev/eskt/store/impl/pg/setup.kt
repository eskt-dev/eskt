package dev.eskt.store.impl.pg

import com.zaxxer.hikari.HikariDataSource

internal actual fun PostgresqlConfig.create() {
    val connectionConfig = connectionConfig.copy(minPoolSize = 1, maxPoolSize = 1)

    val adminConnectionConfig = connectionConfig.copy(database = "postgres")
    val adminHikariConfig = adminConnectionConfig.toHikariConfig()
    val adminDataSource = HikariDataSource(adminHikariConfig)

    adminDataSource.connection.use { connection ->
        connection.prepareStatement("create database ${connectionConfig.database};").use { ps ->
            ps.executeUpdate()
        }
    }

    val hikariConfig = connectionConfig.toHikariConfig()
    val dataSource = HikariDataSource(hikariConfig)

    dataSource.connection.use { connection ->
        streamTypeTableInfoInfoById.values.distinct().forEach { tableInfo ->
            connection.prepareStatement("create schema ${tableInfo.schema};").use { ps ->
                ps.executeUpdate()
            }
            connection.prepareStatement(
                """
                    CREATE TABLE ${tableInfo.schema}.${tableInfo.table}
                    (
                        position    bigserial NOT NULL unique,
                        stream_type text      NOT NULL,
                        stream_id   text      NOT NULL,
                        version     int       NOT NULL,
                        payload     jsonb     NOT NULL,
                        metadata    jsonb     NOT NULL,
                        PRIMARY KEY (stream_id, version)
                    );
                    
                    CREATE OR REPLACE FUNCTION acquire_write_lock()
                        RETURNS trigger AS
                        ${'$'}BODY${'$'}
                        BEGIN
                            EXECUTE pg_advisory_xact_lock(1728994214);
                            RETURN null;
                        END;
                        ${'$'}BODY${'$'}
                        LANGUAGE plpgsql;

                    CREATE TRIGGER event_acquire_lock_before_insert
                        BEFORE INSERT
                        ON ${tableInfo.schema}.${tableInfo.table}
                        FOR EACH STATEMENT
                    EXECUTE PROCEDURE acquire_write_lock();
                """.trimIndent(),
            ).use { ps ->
                ps.executeUpdate()
            }
        }
    }
    dataSource.close()
    adminDataSource.close()
}

internal actual fun PostgresqlConfig.drop() {
    val connectionConfig = connectionConfig.copy(minPoolSize = 1, maxPoolSize = 1)

    val adminConnectionConfig = connectionConfig.copy(database = "postgres")
    val adminHikariConfig = adminConnectionConfig.toHikariConfig()
    val adminDataSource = HikariDataSource(adminHikariConfig)

    adminDataSource.connection.use { connection ->
        connection.prepareStatement("drop database ${connectionConfig.database};").use { ps ->
            ps.executeUpdate()
        }
    }
    adminDataSource.close()
}

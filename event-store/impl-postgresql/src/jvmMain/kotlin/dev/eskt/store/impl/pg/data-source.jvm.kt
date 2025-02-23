package dev.eskt.store.impl.pg

public actual typealias DataSource = javax.sql.DataSource

internal actual fun DataSource.createAdapter(): DatabaseAdapter = JdbcDatabaseAdapter(this)

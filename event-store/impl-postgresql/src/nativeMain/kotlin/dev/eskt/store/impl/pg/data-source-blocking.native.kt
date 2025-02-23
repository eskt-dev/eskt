package dev.eskt.store.impl.pg

public actual interface DataSource

internal actual fun DataSource.createAdapter(): DatabaseAdapter = NativeDatabaseAdapter()

package dev.eskt.store.impl.pg

public expect interface DataSource

internal expect fun DataSource.createAdapter(): dev.eskt.store.impl.pg.blocking.DatabaseAdapter

package dev.eskt.store.impl.pg

@OptIn(ExperimentalStdlibApi::class)
internal expect fun ConnectionConfig.dataSource(closeables: MutableList<AutoCloseable>): DataSource

internal expect fun ConnectionConfig.create(eventTable: String)

internal expect fun ConnectionConfig.drop()

package dev.eskt.store.impl.pg

@Suppress("FunctionName")
public fun PostgresqlEventStore(
    dataSource: DataSource,
    eventTable: String,
    eventWriteTable: String = eventTable,
    block: PostgresqlConfigBuilder<DataSource>.() -> Unit,
): PostgresqlJdbcEventStore = PostgresqlJdbcEventStore(
    PostgresqlConfigBuilder(dataSource, eventTable, eventWriteTable)
        .apply(block)
        .build(),
)

@Suppress("FunctionName")
public fun PostgresqlEventStore(
    dataSource: ReactiveDataSource,
    eventTable: String,
    eventWriteTable: String = eventTable,
    block: PostgresqlConfigBuilder<ReactiveDataSource>.() -> Unit,
): PostgresqlR2dbcEventStore = PostgresqlR2dbcEventStore(
    PostgresqlConfigBuilder(dataSource, eventTable, eventWriteTable)
        .apply(block)
        .build(),
)

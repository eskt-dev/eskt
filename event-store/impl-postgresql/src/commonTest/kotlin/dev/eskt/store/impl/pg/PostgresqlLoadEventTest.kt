package dev.eskt.store.impl.pg

import dev.eskt.store.test.LoadEventTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

internal class PostgresqlLoadEventTest : LoadEventTest<PostgresqlJdbcStorage, PostgresqlJdbcEventStore, PostgresqlStreamTestFactory>(
    PostgresqlStreamTestFactory(),
) {
    @BeforeTest
    fun beforeEach() {
        factory.connectionConfig.create("event")
    }

    @AfterTest
    fun afterEach() {
        factory.closeAll()
        factory.connectionConfig.drop()
    }
}

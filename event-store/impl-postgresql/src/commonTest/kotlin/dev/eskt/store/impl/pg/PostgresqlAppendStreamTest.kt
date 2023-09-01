package dev.eskt.store.impl.pg

import dev.eskt.store.test.AppendStreamTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

internal class PostgresqlAppendStreamTest : AppendStreamTest<PostgresqlStorage, PostgresqlEventStore, PostgresqlStreamTestFactory>(
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

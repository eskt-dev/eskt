package dev.eskt.store.impl.pg

import dev.eskt.store.test.AppendStreamTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

internal class PostgresqlAppendStreamTest : AppendStreamTest<PostgresqlStorage, PostgresqlEventStore, PostgresqlStreamTestFactory>(
    PostgresqlStreamTestFactory(),
) {
    @BeforeTest
    fun beforeEach() {
        factory.config.create()
    }

    @AfterTest
    fun afterEach() {
        factory.stores.forEach { s -> s.close() }
        factory.clear()
        factory.config.drop()
    }
}

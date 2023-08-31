package dev.eskt.store.impl.pg

import dev.eskt.store.impl.common.string.serialization.DefaultEventMetadataSerializer
import dev.eskt.store.test.AppendStreamTest
import dev.eskt.store.test.StreamTestFactory
import dev.eskt.store.test.w.car.CarStreamType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

internal class PostgresqlAppendStreamTest : AppendStreamTest<PostgresqlStorage, PostgresqlEventStore>(
    object : StreamTestFactory<PostgresqlStorage, PostgresqlEventStore>() {
        private val config = PostgresqlConfig(
            registeredTypes = listOf(
                CarStreamType,
            ),
            eventMetadataSerializer = DefaultEventMetadataSerializer,
            connectionConfig = generateTestConnectionConfig(),
        )

        override fun createStorage(): PostgresqlStorage {
            return PostgresqlStorage(config)
        }

        override fun createEventStore(storage: PostgresqlStorage): PostgresqlEventStore {
            return PostgresqlEventStore(config, storage)
        }
    },
) {
    @BeforeTest
    fun beforeEach() {
        val testStorage = factory.newStorage()
        testStorage.config.create()
    }

    @AfterTest
    fun afterEach() {
        factory.stores.forEach { s -> s.close() }
        factory.clear()
        val testStorage = factory.newStorage()
        testStorage.config.drop()
    }
}

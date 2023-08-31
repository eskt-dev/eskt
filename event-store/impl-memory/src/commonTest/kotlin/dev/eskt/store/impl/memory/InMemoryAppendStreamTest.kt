package dev.eskt.store.impl.memory

import dev.eskt.store.test.AppendStreamTest
import dev.eskt.store.test.StreamTestFactory
import dev.eskt.store.test.w.car.CarStreamType

internal class InMemoryAppendStreamTest : AppendStreamTest<InMemoryStorage, InMemoryEventStore>(
    object : StreamTestFactory<InMemoryStorage, InMemoryEventStore>() {
        private val config = InMemoryConfig(
            registeredTypes = listOf(
                CarStreamType,
            ),
        )

        override fun createStorage(): InMemoryStorage {
            return InMemoryStorage(config)
        }

        override fun createEventStore(storage: InMemoryStorage): InMemoryEventStore {
            return InMemoryEventStore(config, storage)
        }
    },
)

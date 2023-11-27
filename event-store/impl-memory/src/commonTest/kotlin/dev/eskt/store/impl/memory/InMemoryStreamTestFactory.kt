package dev.eskt.store.impl.memory

import dev.eskt.store.test.StreamTestFactory
import dev.eskt.store.test.w.car.CarStreamType
import dev.eskt.store.test.w.driver.DriverStreamType

internal class InMemoryStreamTestFactory : StreamTestFactory<InMemoryStorage, InMemoryEventStore>() {
    private val config = InMemoryConfig(
        registeredTypes = listOf(
            CarStreamType,
            DriverStreamType,
        ),
    )

    override fun createStorage(): InMemoryStorage {
        return InMemoryStorage(config)
    }

    override fun createEventStore(storage: InMemoryStorage): InMemoryEventStore {
        return InMemoryEventStore(config, storage)
    }
}

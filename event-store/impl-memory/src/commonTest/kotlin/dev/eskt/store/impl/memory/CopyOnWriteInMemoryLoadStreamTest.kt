package dev.eskt.store.impl.memory

import dev.eskt.store.storage.api.blocking.Storage
import dev.eskt.store.test.LoadStreamTest

internal class CopyOnWriteInMemoryLoadStreamTest : LoadStreamTest<Storage, InMemoryEventStore, InMemoryStreamTestFactory>(
    InMemoryStreamTestFactory(StorageImpl.CopyOnWrite),
)

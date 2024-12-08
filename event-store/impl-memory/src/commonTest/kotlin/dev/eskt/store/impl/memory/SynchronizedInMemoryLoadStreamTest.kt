package dev.eskt.store.impl.memory

import dev.eskt.store.storage.api.Storage
import dev.eskt.store.test.LoadStreamTest

internal class SynchronizedInMemoryLoadStreamTest : LoadStreamTest<Storage, InMemoryEventStore, InMemoryStreamTestFactory>(
    InMemoryStreamTestFactory(StorageImpl.Synchronized),
)

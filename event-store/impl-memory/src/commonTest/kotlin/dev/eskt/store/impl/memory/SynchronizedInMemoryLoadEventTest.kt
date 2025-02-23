package dev.eskt.store.impl.memory

import dev.eskt.store.storage.api.blocking.Storage
import dev.eskt.store.test.LoadEventTest

internal class SynchronizedInMemoryLoadEventTest : LoadEventTest<Storage, InMemoryEventStore, InMemoryStreamTestFactory>(
    InMemoryStreamTestFactory(StorageImpl.Synchronized),
)

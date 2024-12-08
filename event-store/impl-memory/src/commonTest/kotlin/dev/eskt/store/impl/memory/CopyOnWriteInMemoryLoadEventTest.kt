package dev.eskt.store.impl.memory

import dev.eskt.store.storage.api.Storage
import dev.eskt.store.test.LoadEventTest

internal class CopyOnWriteInMemoryLoadEventTest : LoadEventTest<Storage, InMemoryEventStore, InMemoryStreamTestFactory>(
    InMemoryStreamTestFactory(StorageImpl.CopyOnWrite),
)

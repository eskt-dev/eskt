package dev.eskt.store.impl.memory

import dev.eskt.store.storage.api.Storage
import dev.eskt.store.test.AppendStreamTest

internal class CopyOnWriteInMemoryAppendStreamTest : AppendStreamTest<Storage, InMemoryEventStore, InMemoryStreamTestFactory>(
    InMemoryStreamTestFactory(StorageImpl.CopyOnWrite),
)

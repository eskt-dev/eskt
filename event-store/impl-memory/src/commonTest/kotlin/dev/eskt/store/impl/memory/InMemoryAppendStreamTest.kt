package dev.eskt.store.impl.memory

import dev.eskt.store.test.AppendStreamTest

internal class InMemoryAppendStreamTest : AppendStreamTest<InMemoryStorage, InMemoryEventStore, InMemoryStreamTestFactory>(
    InMemoryStreamTestFactory(),
)

package dev.eskt.store.impl.memory

import dev.eskt.store.test.LoadStreamTest

internal class InMemoryLoadStreamTest : LoadStreamTest<InMemoryStorage, InMemoryEventStore, InMemoryStreamTestFactory>(
    InMemoryStreamTestFactory(),
)

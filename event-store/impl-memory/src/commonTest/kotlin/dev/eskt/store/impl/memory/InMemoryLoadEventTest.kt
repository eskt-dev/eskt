package dev.eskt.store.impl.memory

import dev.eskt.store.test.LoadEventTest

internal class InMemoryLoadEventTest : LoadEventTest<InMemoryStorage, InMemoryEventStore, InMemoryStreamTestFactory>(
    InMemoryStreamTestFactory(),
)

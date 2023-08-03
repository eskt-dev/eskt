package dev.eskt.store.memory

import dev.eskt.store.test.LoadStreamTest

internal class InMemoryLoadStreamTest : LoadStreamTest<InMemoryStorage, InMemoryEventStore>(
    { InMemoryStorage() },
    { s -> InMemoryEventStore(s) },
)

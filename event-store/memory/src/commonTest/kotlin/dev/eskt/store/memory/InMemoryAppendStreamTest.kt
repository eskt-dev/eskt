package dev.eskt.store.memory

import dev.eskt.store.test.AppendStreamTest

internal class InMemoryAppendStreamTest : AppendStreamTest<InMemoryStorage, InMemoryEventStore>(
    { InMemoryStorage() },
    { s -> InMemoryEventStore(s) },
)

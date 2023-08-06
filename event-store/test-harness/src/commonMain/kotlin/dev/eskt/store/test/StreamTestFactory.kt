package dev.eskt.store.test

import dev.eskt.store.api.EventStore
import dev.eskt.store.storage.api.Storage

public interface StreamTestFactory<R : Storage, S : EventStore> {
    public fun createStorage(): R
    public fun createEventStore(storage: R): S
}

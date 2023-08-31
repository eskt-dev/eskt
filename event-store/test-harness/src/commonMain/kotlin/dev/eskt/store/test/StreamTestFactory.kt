package dev.eskt.store.test

import dev.eskt.store.api.EventStore
import dev.eskt.store.storage.api.Storage

public abstract class StreamTestFactory<R : Storage, S : EventStore> {

    private val _stores: MutableList<S> = mutableListOf()
    public val stores: List<S>
        get() = _stores

    public fun clear() {
        _stores.clear()
    }

    protected abstract fun createStorage(): R

    public fun newStorage(): R {
        return createStorage()
    }

    protected abstract fun createEventStore(storage: R): S

    public fun newEventStore(storage: R): S {
        return createEventStore(storage).also { _stores.add(it) }
    }
}

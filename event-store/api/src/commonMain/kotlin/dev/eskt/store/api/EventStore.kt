package dev.eskt.store.api

public interface EventStore {
    public val registeredTypes: Set<StreamType<*, *>>
}

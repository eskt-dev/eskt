package dev.eskt.store.api

public interface DatabaseStreamType<I, E> : StreamType<I, E> {
    public val eventTableSchema: String
    public val eventTableName: String
}

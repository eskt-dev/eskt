package dev.eskt.store.api

public interface StringSerializableStreamType<I, E> : StreamType<I, E> {
    public val stringIdSerializer: Serializer<I, String>
    public val stringEventSerializer: Serializer<E, String>
}

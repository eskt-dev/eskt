package dev.eskt.store.api

public interface StringSerializableStreamType<E, I> : StreamType<E, I> {
    public val stringIdSerializer: Serializer<I, String>
    public val stringEventSerializer: Serializer<E, String>
}

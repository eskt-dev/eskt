package dev.eskt.store.api

public interface BinarySerializableStreamType<I, E> : StreamType<I, E> {
    public val stringIdSerializer: Serializer<I, String>
    public val binaryEventSerializer: Serializer<E, ByteArray>
}

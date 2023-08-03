package dev.eskt.store.api

public interface BinarySerializableStreamType<I, E> : StreamType<I, E> {
    public val idSerializer: Serializer<I, String>
    public val binaryEventSerializer: Serializer<E, ByteArray>
}

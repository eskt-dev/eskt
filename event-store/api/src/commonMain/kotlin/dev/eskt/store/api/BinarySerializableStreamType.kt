package dev.eskt.store.api

public interface BinarySerializableStreamType<E, I> : StreamType<E, I> {
    public val stringIdSerializer: Serializer<I, String>
    public val binaryEventSerializer: Serializer<E, ByteArray>
}

package dev.eskt.store.impl.fs

public class CorruptedDataException : RuntimeException {
    internal constructor(message: String) : super(message)
    internal constructor(message: String, cause: Throwable) : super(message, cause)
    internal constructor(cause: Throwable) : super(cause)
}

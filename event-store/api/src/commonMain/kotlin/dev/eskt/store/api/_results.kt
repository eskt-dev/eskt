package dev.eskt.store.api

import kotlin.jvm.JvmInline

public sealed interface Result<out R, out F : Exception> {
    @JvmInline
    public value class Ok<out R>(
        public val result: R,
    ) : Result<R, Nothing>

    @JvmInline
    public value class Failure<out F : Exception>(
        public val reason: F,
    ) : Result<Nothing, F>

    public fun unwrap(): R = when (this) {
        is Ok -> result
        is Failure -> throw reason
    }

    public open class FailureException : RuntimeException()
}

public sealed class AppendFailure : Result.FailureException() {
    public data class ExpectedVersionMismatch(val currentVersion: Int, val expectedVersion: Int) : AppendFailure()
}

public sealed class LoadFailure : Result.FailureException()

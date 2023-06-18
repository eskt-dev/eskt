package dev.eskt.store

import kotlin.jvm.JvmInline

sealed interface Result<out R, out F : Exception> {
    @JvmInline
    value class Ok<out R>(val result: R) : Result<R, Nothing>

    @JvmInline
    value class Failure<out F : Exception>(val reason: F) : Result<Nothing, F>

    fun unwrap(): R = when (this) {
        is Ok -> result
        is Failure -> throw reason
    }

    open class FailureException : RuntimeException()
}

sealed class AppendFailure : Result.FailureException() {
    data class ExpectedVersionMismatch(val currentVersion: Int, val expectedVersion: Int) : AppendFailure()
}

sealed class LoadFailure : Result.FailureException()

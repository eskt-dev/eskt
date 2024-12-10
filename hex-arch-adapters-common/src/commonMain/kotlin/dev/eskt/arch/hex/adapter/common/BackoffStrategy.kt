package dev.eskt.arch.hex.adapter.common

import kotlin.time.Duration

public fun interface BackoffStrategy {
    /**
     * Provides the backoff duration before the given n-th retry.
     * The [retry] parameter represents the retry attempt and is guaranteed to be >= 1,
     * where the first retry corresponds to retry = 1, the second retry to retry = 2, and so on.
     */
    public fun backoff(retry: Int): Duration

    public data class Constant(val duration: Duration) : BackoffStrategy {
        init {
            check(duration.isPositive()) // enforcing a non-zero value here since zero will cause busy-waiting
        }

        override fun backoff(retry: Int): Duration = duration
    }
}

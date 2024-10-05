package dev.eskt.arch.hex.adapter.common

import kotlin.time.Duration.Companion.seconds

public data class EventListenerExecutorConfig(
    public val threadPoolName: String = "evt-listener",
    public val threadPoolSize: Int = 4,
    public val batchSize: Int = 100,
    public val errorBackoff: BackoffStrategy = BackoffStrategy.Constant(15.seconds),
)

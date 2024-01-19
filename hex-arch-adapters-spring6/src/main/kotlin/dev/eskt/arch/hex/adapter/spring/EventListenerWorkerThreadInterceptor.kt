package dev.eskt.arch.hex.adapter.spring

public interface EventListenerWorkerThreadInterceptor {
    public fun intercept(work: () -> Unit)
}

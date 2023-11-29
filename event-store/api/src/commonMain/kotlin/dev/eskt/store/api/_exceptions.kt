package dev.eskt.store.api

public data class StreamVersionMismatchException(val currentVersion: Int, val expectedVersion: Int) : RuntimeException()

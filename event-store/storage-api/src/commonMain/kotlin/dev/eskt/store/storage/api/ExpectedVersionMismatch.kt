package dev.eskt.store.storage.api

public data class ExpectedVersionMismatch(
    val currentVersion: Int,
    val expectedVersion: Int,
) : Exception("Expected version is $expectedVersion, but current version is $currentVersion")

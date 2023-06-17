package dev.eskt

sealed interface AppendResult {
    data class Appended(val currentVersion: Int) : AppendResult
    data class ExpectedVersionMismatch(val currentVersion: Int, val expectedVersion: Int) : AppendResult
}

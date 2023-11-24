package dev.eskt.example.domain

interface UnitOfWork {
    fun <T> mark(action: () -> T): T
}

package dev.eskt.arch.hex.adapter.common

public interface Bookmark {
    public fun get(id: String): Long
    public fun set(id: String, value: Long)
}

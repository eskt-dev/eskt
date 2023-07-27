package dev.eskt.store.fs

import okio.FileHandle

internal fun FileHandle.readLong(offset: Long): Long {
    val buff = ByteArray(8)
    read(offset, buff, 0, 8)
    return buff.foldIndexed(0L) { index, acc, byte -> acc + (byte.toLong() shl (64 - (index + 1) * 8)) }
}

internal fun FileHandle.readInt(offset: Long): Int {
    val buff = ByteArray(4)
    read(offset, buff, 0, 4)
    return buff.foldIndexed(0) { index, acc, byte -> acc + (byte.toInt() shl (32 - (index + 1) * 8)) }
}

package dev.eskt.store.impl.fs

import okio.FileSystem

internal actual fun eventStoreFileSystem(): FileSystem {
    return FileSystem.SYSTEM
}

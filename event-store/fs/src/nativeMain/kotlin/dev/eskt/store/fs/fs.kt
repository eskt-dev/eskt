package dev.eskt.store.fs

import okio.FileSystem

internal actual fun eventStoreFileSystem(): FileSystem {
    return FileSystem.SYSTEM
}

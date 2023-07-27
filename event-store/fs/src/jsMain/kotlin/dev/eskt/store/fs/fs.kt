package dev.eskt.store.fs

import okio.FileSystem
import okio.NodeJsFileSystem

internal actual fun eventStoreFileSystem(): FileSystem {
    return NodeJsFileSystem
}

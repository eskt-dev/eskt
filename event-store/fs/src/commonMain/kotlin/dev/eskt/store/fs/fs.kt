package dev.eskt.store.fs

import okio.FileSystem

internal expect fun eventStoreFileSystem(): FileSystem

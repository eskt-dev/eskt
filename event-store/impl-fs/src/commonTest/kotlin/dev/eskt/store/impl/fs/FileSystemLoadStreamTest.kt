package dev.eskt.store.impl.fs

import dev.eskt.store.test.LoadStreamTest

internal class FileSystemLoadStreamTest : LoadStreamTest<FileSystemStorage, FileSystemEventStore, FileSystemStreamTestFactory>(
    FileSystemStreamTestFactory(),
)

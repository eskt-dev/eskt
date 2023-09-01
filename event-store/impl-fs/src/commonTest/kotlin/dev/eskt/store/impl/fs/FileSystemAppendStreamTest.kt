package dev.eskt.store.impl.fs

import dev.eskt.store.test.AppendStreamTest

internal class FileSystemAppendStreamTest : AppendStreamTest<FileSystemStorage, FileSystemEventStore, FileSystemStreamTestFactory>(
    FileSystemStreamTestFactory(),
)

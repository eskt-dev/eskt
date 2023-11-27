package dev.eskt.store.impl.fs

import dev.eskt.store.test.LoadEventTest

internal class FileSystemLoadEventTest : LoadEventTest<FileSystemStorage, FileSystemEventStore, FileSystemStreamTestFactory>(
    FileSystemStreamTestFactory(),
)

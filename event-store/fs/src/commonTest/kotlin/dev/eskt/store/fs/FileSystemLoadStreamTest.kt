package dev.eskt.store.fs

import dev.eskt.store.test.LoadStreamTest
import dev.eskt.store.test.w.car.CarStreamType

internal class FileSystemLoadStreamTest : LoadStreamTest<FileSystemStorage, FileSystemEventStore>(
    { FileSystemStorage(createEmptyTemporaryFolder()) },
    { s ->
        FileSystemEventStore(s) {
            registerStreamType(CarStreamType)
        }
    },
)

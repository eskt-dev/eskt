package dev.eskt.store.fs

import dev.eskt.store.test.AppendStreamTest
import dev.eskt.store.test.w.car.CarStreamType

internal class FileSystemAppendStreamTest : AppendStreamTest<FileSystemStorage, FileSystemEventStore>(
    { FileSystemStorage(createEmptyTemporaryFolder()) },
    { s ->
        FileSystemEventStore(s) {
            registerStreamType(CarStreamType)
        }
    },
)

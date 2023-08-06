package dev.eskt.store.impl.fs

import dev.eskt.store.impl.common.binary.serialization.DefaultEventMetadataSerializer
import dev.eskt.store.test.LoadStreamTest
import dev.eskt.store.test.StreamTestFactory
import dev.eskt.store.test.w.car.CarStreamType

internal class FileSystemLoadStreamTest : LoadStreamTest<FileSystemStorage, FileSystemEventStore>(
    object : StreamTestFactory<FileSystemStorage, FileSystemEventStore> {
        private val config = FileSystemConfig(
            basePath = createEmptyTemporaryFolder(),
            eventMetadataSerializer = DefaultEventMetadataSerializer,
            registeredTypes = listOf(
                CarStreamType,
            ),
        )

        override fun createStorage(): FileSystemStorage {
            return FileSystemStorage(config)
        }

        override fun createEventStore(storage: FileSystemStorage): FileSystemEventStore {
            return FileSystemEventStore(config, storage)
        }
    },
)

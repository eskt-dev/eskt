package dev.eskt.store.impl.fs

import dev.eskt.store.impl.common.binary.serialization.DefaultEventMetadataSerializer
import dev.eskt.store.test.StreamTestFactory
import dev.eskt.store.test.w.car.CarStreamType
import dev.eskt.store.test.w.driver.DriverStreamType

internal class FileSystemStreamTestFactory : StreamTestFactory<FileSystemStorage, FileSystemEventStore>() {
    private val config = FileSystemConfig(
        basePath = createEmptyTemporaryFolder(),
        eventMetadataSerializer = DefaultEventMetadataSerializer,
        registeredTypes = listOf(
            CarStreamType,
            DriverStreamType,
        ),
        payloadSerializers = mapOf(
            CarStreamType to CarStreamType.binaryEventSerializer,
            DriverStreamType to DriverStreamType.binaryEventSerializer,
        ),
        idSerializers = mapOf(
            CarStreamType to CarStreamType.stringIdSerializer,
            DriverStreamType to DriverStreamType.stringIdSerializer,
        ),
    )

    override fun createStorage(): FileSystemStorage {
        return FileSystemStorage(config)
    }

    override fun createEventStore(storage: FileSystemStorage): FileSystemEventStore {
        return FileSystemEventStore(config, storage)
    }
}

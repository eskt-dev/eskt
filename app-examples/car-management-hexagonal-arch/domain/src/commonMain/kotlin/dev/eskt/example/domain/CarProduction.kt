package dev.eskt.example.domain

import com.benasher44.uuid.Uuid
import dev.eskt.example.domain.command.Car
import dev.eskt.example.domain.command.CarCommand
import dev.eskt.example.domain.command.CarWriteHelperRepository
import dev.eskt.example.domain.command.handle
import dev.eskt.store.api.EventStore
import dev.eskt.store.test.w.car.CarEvent
import dev.eskt.store.test.w.car.CarStreamType

@UseCase
class CarProduction(
    private val carWriteHelperRepository: CarWriteHelperRepository,
    private val eventStore: EventStore,
    private val unitOfWork: UnitOfWork,
) {
    fun produceCar(command: CarCommand.Produce): Uuid = unitOfWork.mark {
        val writeSideCar = carWriteHelperRepository.getOrCreate(command.id, command.vin)
        if (writeSideCar.id != command.id) {
            return@mark writeSideCar.id
        }

        val carStreamHandler = eventStore.withStreamType(CarStreamType)

        val loadResult = carStreamHandler
            .loadStream(writeSideCar.id)
            .unwrap()

        val car = loadResult
            .map { it.event }
            .fold(Car(writeSideCar.id)) { s: Car, e: CarEvent -> s + e }

        val events = car.handle(command)

        carStreamHandler
            .appendStream(writeSideCar.id, car.version, events)
            .unwrap()

        writeSideCar.id
    }
}

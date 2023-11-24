package dev.eskt.example.domain.command

import dev.eskt.store.test.w.car.CarEvent
import dev.eskt.store.test.w.car.CarProducedEvent

fun Car.handle(command: CarCommand.Produce): List<CarEvent> {
    if (version > 0) {
        throw IllegalStateException("Car with id $id already exists")
    }
    return listOf(
        CarProducedEvent(
            vin = command.vin,
            producer = command.producer,
            make = command.make,
            model = command.model,
        )
    )
}

package dev.eskt.example.app

import com.benasher44.uuid.uuidFrom
import dev.eskt.example.domain.CarProduction
import dev.eskt.example.domain.command.CarCommand
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CarController(
    val carProduction: CarProduction,
) {
    @PostMapping("/cars")
    fun postCar(
        @RequestBody body: ProduceCarRequest,
    ) {
        carProduction.produceCar(
            CarCommand.Produce(
                uuidFrom(body.id),
                body.vin,
                body.producer,
                body.make,
                body.model,
            )
        )
    }

    data class ProduceCarRequest(
        val id: String,
        val vin: String,
        val producer: Long,
        val make: String,
        val model: String,
    )
}

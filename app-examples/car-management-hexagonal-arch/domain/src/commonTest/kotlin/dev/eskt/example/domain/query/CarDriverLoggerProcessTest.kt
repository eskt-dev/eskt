package dev.eskt.example.domain.query

import com.benasher44.uuid.Uuid
import dev.eskt.example.domain.process.CarDriverLogger
import dev.eskt.example.domain.process.CarProductionNotificationProcess
import dev.eskt.example.domain.process.CarProductionNotifier
import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.StreamType
import dev.eskt.store.test.w.car.CarProducedEvent
import dev.eskt.store.test.w.car.CarStreamType
import dev.eskt.store.test.w.driver.DriverRegisteredEvent
import dev.eskt.store.test.w.driver.DriverStreamType
import kotlin.test.Test
import kotlin.test.assertEquals

class CarDriverLoggerProcessTest {
    @Test
    fun `given two different streams - when listening events - then both events are received`() {
        val logger = object: CarDriverLogger {
            override val id: String = "car-driver-pm"
            val logs = mutableListOf<String>()

            override val streamTypes: List<StreamType<out Any, Uuid>> = listOf(
                CarStreamType,
                DriverStreamType,
            )

            override fun listen(envelope: EventEnvelope<Any, Uuid>) {
                when (val event = envelope.event) {
                    is DriverRegisteredEvent -> logs += "Driver registered: ${event.name}"
                    is CarProducedEvent -> logs += "Car produced: ${event.make} ${event.model}"
                }
            }
        }

        logger.listen(
            EventEnvelope(
                streamType = CarStreamType,
                streamId = car1StreamId,
                version = 1,
                position = 1,
                metadata = emptyMap(),
                event = CarProducedEvent(
                    vin = "2A4RR5D1XAR410299",
                    make = "Kia",
                    model = "Rio",
                    producer = 1,
                ),
            ),
        )
        logger.listen(
            EventEnvelope(
                streamType = DriverStreamType,
                streamId = driver1StreamId,
                version = 1,
                position = 2,
                metadata = emptyMap(),
                event = DriverRegisteredEvent(
                    licence = "1",
                    name = "Mr. Driver",
                ),
            ),
        )

        assertEquals(2, logger.logs.size)
    }
}

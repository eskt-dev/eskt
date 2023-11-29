package dev.eskt.example.domain.query

import dev.eskt.example.domain.process.CarProductionNotificationProcess
import dev.eskt.example.domain.process.CarProductionNotifier
import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.test.w.car.CarProducedEvent
import dev.eskt.store.test.w.car.CarStreamType
import kotlin.test.Test
import kotlin.test.assertEquals

class CarProductionNotificationProcessTest {

    @Test
    fun `given no cars - when listening production event - then notifier is called correctly`() {
        // given

        // when
        lateinit var notifiedVin: String
        val notifier = object : CarProductionNotifier {
            override fun notify(vin: String, make: String, model: String) {
                notifiedVin = vin
            }
        }
        val listener = CarProductionNotificationProcess(notifier)
        listener.listen(
            EventEnvelope(
                streamType = CarStreamType, streamId = car1StreamId, version = 1, position = 1,
                metadata = emptyMap(),
                event = CarProducedEvent(
                    vin = "2A4RR5D1XAR410299",
                    make = "Kia",
                    model = "Rio",
                    producer = 1,
                ),
            ),
        )

        // then
        assertEquals("2A4RR5D1XAR410299", notifiedVin)
    }
}

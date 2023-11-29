package dev.eskt.example.domain.process

import com.benasher44.uuid.Uuid
import dev.eskt.arch.hex.port.SingleStreamTypeEventListener
import dev.eskt.example.domain.EventListener
import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.StreamType
import dev.eskt.store.test.w.car.CarEvent
import dev.eskt.store.test.w.car.CarProducedEvent
import dev.eskt.store.test.w.car.CarStreamType

@EventListener
class CarProductionNotificationProcess(
    private val notifier: CarProductionNotifier,
) : SingleStreamTypeEventListener<CarEvent, Uuid> {
    override val id: String = "car-production-notification-process"
    override val streamType: StreamType<CarEvent, Uuid> = CarStreamType

    override fun listen(envelope: EventEnvelope<CarEvent, Uuid>) {
        when (val event = envelope.event) {
            is CarProducedEvent -> notifier.notify(vin = event.vin, make = event.make, model = event.model)
            else -> {}
        }
    }
}

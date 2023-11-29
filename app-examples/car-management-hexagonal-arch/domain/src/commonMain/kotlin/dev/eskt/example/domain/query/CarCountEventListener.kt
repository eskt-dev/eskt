package dev.eskt.example.domain.query

import com.benasher44.uuid.Uuid
import dev.eskt.arch.hex.port.SingleStreamTypeEventListener
import dev.eskt.example.domain.EventListener
import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.StreamType
import dev.eskt.store.test.w.car.CarEliminatedEvent
import dev.eskt.store.test.w.car.CarEvent
import dev.eskt.store.test.w.car.CarProducedEvent
import dev.eskt.store.test.w.car.CarStreamType

@EventListener
class CarCountEventListener(
    private val makeModelCarCountRepository: MakeModelCarCountRepository,
    private val makeModelCarRepository: MakeModelCarRepository,
) : SingleStreamTypeEventListener<CarEvent, Uuid> {
    override val id: String = "car-count-read-model"
    override val streamType: StreamType<CarEvent, Uuid> = CarStreamType

    override fun listen(envelope: EventEnvelope<CarEvent, Uuid>) {
        when (val event = envelope.event) {
            is CarProducedEvent -> {
                val car = makeModelCarRepository.find(envelope.streamId)
                if (car != null) return // already processed
                makeModelCarRepository.add(MakeModelCar(id = envelope.streamId, make = event.make, model = event.model))

                val makeModelCount = makeModelCarCountRepository.find(make = event.make, model = event.model)
                    ?: MakeModelCarCount(make = event.make, model = event.model, count = 0)

                makeModelCarCountRepository.save(makeModelCount.copy(count = makeModelCount.count + 1))
            }

            is CarEliminatedEvent -> {
                val car = makeModelCarRepository.find(envelope.streamId)
                    ?: return // already processed
                makeModelCarRepository.removeById(envelope.streamId)

                val makeModelCount = makeModelCarCountRepository.find(make = car.make, model = car.model)
                    ?: throw IllegalStateException("Count should exist as car ${envelope.streamId} is being eliminated")

                makeModelCarCountRepository.save(makeModelCount.copy(count = makeModelCount.count - 1))
            }

            else -> {}
        }
    }
}

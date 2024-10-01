package dev.eskt.example.domain.process

import com.benasher44.uuid.Uuid
import dev.eskt.arch.hex.port.MultiStreamTypeEventListener
import dev.eskt.example.domain.EventListener
import dev.eskt.store.api.EventEnvelope
import dev.eskt.store.api.StreamType
import dev.eskt.store.test.w.car.CarProducedEvent
import dev.eskt.store.test.w.car.CarStreamType
import dev.eskt.store.test.w.driver.DriverRegisteredEvent
import dev.eskt.store.test.w.driver.DriverStreamType

@EventListener
interface CarDriverLogger : MultiStreamTypeEventListener<Any, Uuid>

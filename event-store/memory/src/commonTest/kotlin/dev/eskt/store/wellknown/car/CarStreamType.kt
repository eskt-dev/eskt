package dev.eskt.store.wellknown.car

import dev.eskt.store.StreamType

internal object CarStreamType : StreamType<String, CarEvent> {
    override val id: String = "Car"
}

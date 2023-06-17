package dev.eskt.wellknown.car

import dev.eskt.StreamType

internal object CarStreamType : StreamType<String, CarEvent> {
    override val id: String = "Car"
}

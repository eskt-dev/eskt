package dev.eskt.store.test.w.car

import dev.eskt.store.StreamType

public object CarStreamType : StreamType<String, CarEvent> {
    override val id: String = "Car"
}

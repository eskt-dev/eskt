package dev.eskt.store.test.w.part

import dev.eskt.store.api.StreamType

public object PartStreamType : StreamType<PartEvent, String> {
    override val id: String = "Part"
}

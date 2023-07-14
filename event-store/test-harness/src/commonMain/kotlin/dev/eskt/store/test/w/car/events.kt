package dev.eskt.store.test.w.car

public sealed interface CarEvent

public data class CarProducedEvent(
    val vin: String,
    val producer: Long,
    val make: String,
    val model: String,
) : CarEvent

public data class CarSoldEvent(
    val seller: Long,
    val buyer: Long,
    val price: Float,
) : CarEvent

public data class CarDrivenEvent(
    val distance: Double,
) : CarEvent

public data class CarEliminatedEvent(
    val reason: EliminationReason,
) : CarEvent

public enum class EliminationReason {
    Unfixable,
    Lost,
    Stolen,
}

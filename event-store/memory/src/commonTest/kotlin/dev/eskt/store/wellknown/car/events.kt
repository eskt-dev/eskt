package dev.eskt.store.wellknown.car

internal sealed interface CarEvent

internal data class CarProducedEvent(
    val vin: String,
    val producer: Long,
    val make: String,
    val model: String,
) : CarEvent

internal data class CarSoldEvent(
    val seller: Long,
    val buyer: Long,
    val price: Float,
) : CarEvent

internal data class CarDrivenEvent(
    val distance: Double,
) : CarEvent

internal data class CarEliminatedEvent(
    val reason: EliminationReason,
) : CarEvent

internal enum class EliminationReason {
    Unfixable,
    Lost,
    Stolen,
}

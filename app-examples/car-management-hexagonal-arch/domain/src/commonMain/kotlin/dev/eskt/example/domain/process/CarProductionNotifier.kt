package dev.eskt.example.domain.process

interface CarProductionNotifier {
    fun notify(vin: String, make: String, model: String)
}

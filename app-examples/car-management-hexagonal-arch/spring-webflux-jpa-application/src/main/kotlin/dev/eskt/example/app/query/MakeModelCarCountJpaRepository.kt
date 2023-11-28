package dev.eskt.example.app.query

import dev.eskt.example.domain.query.MakeModelCarCount
import dev.eskt.example.domain.query.MakeModelCarCountRepository
import org.springframework.data.jpa.repository.JpaRepository
import kotlin.jvm.optionals.getOrNull

interface MakeModelCarCountJpaRepository : JpaRepository<MakeModelCarCountEntity, MakeModelCarCountEntity.Id>, MakeModelCarCountRepository {
    override fun listAll(): List<MakeModelCarCount> {
        return findAll().map { it.toDomain() }
    }

    override fun find(make: String, model: String): MakeModelCarCount? {
        return findById(MakeModelCarCountEntity.Id(make, model)).getOrNull()?.toDomain()
    }

    override fun save(carCount: MakeModelCarCount) {
        save(MakeModelCarCountEntity(MakeModelCarCountEntity.Id(make = carCount.make, model = carCount.model), count = carCount.count))
    }

    private fun MakeModelCarCountEntity.toDomain() = MakeModelCarCount(
        make = id.make,
        model = id.model,
        count = count,
    )
}

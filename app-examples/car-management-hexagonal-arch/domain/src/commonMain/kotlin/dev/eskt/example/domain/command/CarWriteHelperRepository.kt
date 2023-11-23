package dev.eskt.example.domain.command

import com.benasher44.uuid.Uuid

interface CarWriteHelperRepository {
    fun getByUuid(id: Uuid): CarWriteHelper?
    fun getOrCreate(id: Uuid, vin: String): CarWriteHelper
}

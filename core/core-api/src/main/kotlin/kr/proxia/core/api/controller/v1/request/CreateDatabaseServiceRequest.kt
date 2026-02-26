package kr.proxia.core.api.controller.v1.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.proxia.core.domain.CreateDatabaseService
import kr.proxia.core.enums.DatabaseEngine

data class CreateDatabaseServiceRequest(
    @field:NotBlank
    val name: String,
    @field:NotNull
    val x: Double,
    @field:NotNull
    val y: Double,
    @field:NotNull
    val engine: DatabaseEngine,
) {
    fun toDomain(): CreateDatabaseService =
        CreateDatabaseService(
            name = name,
            x = x,
            y = y,
            engine = engine,
        )
}

package kr.proxia.domain.service.presentation.response

import kr.proxia.domain.service.domain.entity.ServiceEntity
import kr.proxia.domain.service.domain.enums.ServiceType
import java.time.LocalDateTime

data class ServiceResponse(
    val id: Long,
    val projectId: Long,
    val name: String,
    val description: String?,
    val type: ServiceType,
    val x: Double,
    val y: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(service: ServiceEntity) =
            ServiceResponse(
                id = service.id,
                projectId = service.projectId,
                name = service.name,
                description = service.description,
                type = service.type,
                x = service.x,
                y = service.y,
                createdAt = service.createdAt,
                updatedAt = service.updatedAt,
            )
    }
}

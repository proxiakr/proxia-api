package kr.proxia.domain.service.presentation.response

import kr.proxia.domain.resource.presentation.response.AppResourceResponse
import kr.proxia.domain.resource.presentation.response.DatabaseResourceResponse
import kr.proxia.domain.resource.presentation.response.DomainResourceResponse
import kr.proxia.domain.service.domain.entity.ServiceEntity
import kr.proxia.domain.service.domain.enums.ServiceType
import java.time.LocalDateTime
import java.util.UUID

data class ServiceResponse(
    val id: UUID,
    val projectId: UUID,
    val name: String,
    val description: String?,
    val type: ServiceType,
    val x: Double,
    val y: Double,
    val targetId: UUID?,
    val appResource: AppResourceResponse?,
    val databaseResource: DatabaseResourceResponse?,
    val domainResource: DomainResourceResponse?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun of(
            service: ServiceEntity,
            appResource: AppResourceResponse? = null,
            databaseResource: DatabaseResourceResponse? = null,
            domainResource: DomainResourceResponse? = null,
        ) = ServiceResponse(
            id = service.id,
            projectId = service.projectId,
            name = service.name,
            description = service.description,
            type = service.type,
            x = service.x,
            y = service.y,
            targetId = service.targetId,
            appResource = appResource,
            databaseResource = databaseResource,
            domainResource = domainResource,
            createdAt = service.createdAt,
            updatedAt = service.updatedAt,
        )
    }
}

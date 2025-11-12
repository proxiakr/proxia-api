package kr.proxia.domain.service.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.service.presentation.request.CreateServiceRequest
import kr.proxia.domain.service.presentation.request.UpdateServicePositionRequest
import kr.proxia.domain.service.presentation.request.UpdateServiceRequest
import kr.proxia.domain.service.presentation.response.ServiceResponse
import java.util.UUID

@Tag(name = "Service")
interface ServiceDocs {
    @Operation(summary = "Create Service")
    fun createService(
        projectId: UUID,
        request: CreateServiceRequest,
    )

    @Operation(summary = "Get Services")
    fun getServices(projectId: UUID): List<ServiceResponse>

    @Operation(summary = "Get Service")
    fun getService(
        projectId: UUID,
        serviceId: UUID,
    ): ServiceResponse

    @Operation(summary = "Update Service")
    fun updateService(
        projectId: UUID,
        serviceId: UUID,
        request: UpdateServiceRequest,
    )

    @Operation(summary = "Update Service Position")
    fun updateServicePosition(
        projectId: UUID,
        serviceId: UUID,
        request: UpdateServicePositionRequest,
    )

    @Operation(summary = "Delete Service")
    fun deleteService(
        projectId: UUID,
        serviceId: UUID,
    )
}

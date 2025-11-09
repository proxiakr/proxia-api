package kr.proxia.domain.service.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.service.presentation.request.CreateServiceRequest
import kr.proxia.domain.service.presentation.request.UpdateServicePositionRequest
import kr.proxia.domain.service.presentation.request.UpdateServiceRequest
import kr.proxia.domain.service.presentation.response.ServiceResponse

@Tag(name = "Service")
interface ServiceDocs {
    @Operation(summary = "Create Service")
    fun createService(
        projectId: Long,
        request: CreateServiceRequest,
    )

    @Operation(summary = "Get Services")
    fun getServices(projectId: Long): List<ServiceResponse>

    @Operation(summary = "Get Service")
    fun getService(
        projectId: Long,
        serviceId: Long,
    ): ServiceResponse

    @Operation(summary = "Update Service")
    fun updateService(
        projectId: Long,
        serviceId: Long,
        request: UpdateServiceRequest,
    )

    @Operation(summary = "Update Service Position")
    fun updateServicePosition(
        projectId: Long,
        serviceId: Long,
        request: UpdateServicePositionRequest,
    )

    @Operation(summary = "Delete Service")
    fun deleteService(
        projectId: Long,
        serviceId: Long,
    )
}

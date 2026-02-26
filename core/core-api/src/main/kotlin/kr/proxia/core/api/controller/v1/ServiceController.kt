package kr.proxia.core.api.controller.v1

import kr.proxia.core.api.controller.v1.request.CreateAppServiceRequest
import kr.proxia.core.api.controller.v1.request.CreateDatabaseServiceRequest
import kr.proxia.core.api.controller.v1.response.ServiceDetailResponse
import kr.proxia.core.api.controller.v1.response.ServiceResponse
import kr.proxia.core.api.controller.v1.response.ServiceTypeResponse
import kr.proxia.core.domain.ServiceService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/projects/{projectId}/services")
class ServiceController(
    private val serviceService: ServiceService,
) {
    @GetMapping
    fun getServices(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable workspaceId: UUID,
        @PathVariable projectId: UUID,
    ): List<ServiceResponse> {
        val services = serviceService.getServices(userId, workspaceId, projectId)

        return services.map {
            ServiceResponse(
                id = it.id,
                name = it.name,
                x = it.x,
                y = it.y,
                status = it.status.name,
            )
        }
    }

    @GetMapping("/{serviceId}")
    fun getService(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable workspaceId: UUID,
        @PathVariable projectId: UUID,
        @PathVariable serviceId: UUID,
    ): ServiceDetailResponse {
        TODO()
    }

    @PostMapping("/app")
    fun createAppService(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable workspaceId: UUID,
        @PathVariable projectId: UUID,
        @RequestBody request: CreateAppServiceRequest,
    ): ServiceResponse {
        val service = serviceService.createAppService(userId, workspaceId, projectId, request.toDomain())

        return ServiceResponse(
            id = service.id,
            name = service.name,
            x = service.x,
            y = service.y,
            status = service.status.name,
        )
    }

    @PostMapping("/database")
    fun createDatabaseService(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable workspaceId: UUID,
        @PathVariable projectId: UUID,
        @RequestBody request: CreateDatabaseServiceRequest,
    ): ServiceResponse {
        val service = serviceService.createDatabaseService(userId, workspaceId, projectId, request.toDomain())

        return ServiceResponse(
            id = service.id,
            name = service.name,
            x = service.x,
            y = service.y,
            status = service.status.name,
        )
    }

    @DeleteMapping("/{serviceId}")
    fun deleteService(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable workspaceId: UUID,
        @PathVariable projectId: UUID,
        @PathVariable serviceId: UUID,
    ) {
        serviceService.deleteService(userId, projectId, serviceId)
    }
}

package kr.proxia.domain.service.presentation.controller

import jakarta.validation.Valid
import kr.proxia.domain.service.application.service.ServiceService
import kr.proxia.domain.service.presentation.docs.ServiceDocs
import kr.proxia.domain.service.presentation.request.CreateServiceRequest
import kr.proxia.domain.service.presentation.request.UpdateServicePositionRequest
import kr.proxia.domain.service.presentation.request.UpdateServiceRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/projects/{projectId}/services")
class ServiceController(
    private val serviceService: ServiceService,
) : ServiceDocs {
    @PostMapping
    override fun createService(
        @PathVariable projectId: UUID,
        @Valid @RequestBody request: CreateServiceRequest,
    ) = serviceService.createService(projectId, request)

    @GetMapping
    override fun getServices(
        @PathVariable projectId: UUID,
    ) = serviceService.getServices(projectId)

    @GetMapping("/{serviceId}")
    override fun getService(
        @PathVariable projectId: UUID,
        @PathVariable serviceId: UUID,
    ) = serviceService.getService(serviceId)

    @PutMapping("/{serviceId}")
    override fun updateService(
        @PathVariable projectId: UUID,
        @PathVariable serviceId: UUID,
        @Valid @RequestBody request: UpdateServiceRequest,
    ) = serviceService.updateService(serviceId, request)

    @PatchMapping("/{serviceId}/position")
    override fun updateServicePosition(
        @PathVariable projectId: UUID,
        @PathVariable serviceId: UUID,
        @Valid @RequestBody request: UpdateServicePositionRequest,
    ) = serviceService.updateServicePosition(serviceId, request)

    @DeleteMapping("/{serviceId}")
    override fun deleteService(
        @PathVariable projectId: UUID,
        @PathVariable serviceId: UUID,
    ) = serviceService.deleteService(serviceId)
}

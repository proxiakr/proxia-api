package kr.proxia.domain.deployment.presentation.controller

import kr.proxia.domain.deployment.application.service.DeploymentService
import kr.proxia.domain.deployment.presentation.request.DeployRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/services")
class DeploymentController(
    private val deploymentService: DeploymentService,
) {
    @PostMapping("/{serviceId}/deploy")
    fun deployService(
        @PathVariable serviceId: Long,
        @RequestBody request: DeployRequest?,
    ): ResponseEntity<Map<String, String>> {
        deploymentService.deploy(serviceId, request?.branch)
        return ResponseEntity.ok(mapOf("message" to "Deployment started"))
    }
}

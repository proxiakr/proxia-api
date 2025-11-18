package kr.proxia.domain.deployment.presentation.controller

import kr.proxia.domain.deployment.application.service.DeploymentService
import kr.proxia.domain.deployment.presentation.request.DeployRequest
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/services/{serviceId}/deployments")
class DeploymentController(
    private val deploymentService: DeploymentService,
) {
    @PostMapping
    fun deployService(
        @PathVariable("serviceId") serviceId: UUID,
        @RequestBody request: DeployRequest,
    ) = deploymentService.deploy(serviceId, request)
}

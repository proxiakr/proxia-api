package kr.proxia.domain.deployment.presentation.controller

import kr.proxia.domain.deployment.application.service.DeploymentService
import kr.proxia.domain.deployment.presentation.docs.DeploymentDocs
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/projects/{projectId}/services/{serviceId}/deployments")
class DeploymentController(
    private val deploymentService: DeploymentService,
) : DeploymentDocs {
    @PostMapping
    suspend fun deployApp(
        @PathVariable projectId: UUID,
        @PathVariable serviceId: UUID,
    ) = deploymentService.deployApp(projectId, serviceId)
}

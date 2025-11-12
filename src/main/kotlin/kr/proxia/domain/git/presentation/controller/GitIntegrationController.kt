package kr.proxia.domain.git.presentation.controller

import kr.proxia.domain.git.application.service.GitIntegrationService
import kr.proxia.domain.git.presentation.docs.GitIntegrationDocs
import kr.proxia.domain.git.presentation.request.CreateGitIntegrationRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/git/integrations")
class GitIntegrationController(
    private val gitIntegrationService: GitIntegrationService,
) : GitIntegrationDocs {
    @PostMapping
    override fun createGitIntegration(
        @RequestBody request: CreateGitIntegrationRequest,
    ) = gitIntegrationService.createGitIntegration(request)

    @DeleteMapping("/{integrationId}")
    override fun deleteGitIntegration(
        @PathVariable integrationId: UUID,
    ) = gitIntegrationService.deleteGitIntegration(integrationId)
}

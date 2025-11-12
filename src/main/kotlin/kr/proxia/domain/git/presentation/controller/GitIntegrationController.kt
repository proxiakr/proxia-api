package kr.proxia.domain.git.presentation.controller

import kr.proxia.domain.git.application.service.GitIntegrationService
import kr.proxia.domain.git.presentation.docs.GitIntegrationDocs
import kr.proxia.domain.git.presentation.request.CreateGitIntegrationRequest
import kr.proxia.domain.git.presentation.response.GitIntegrationResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/git/integrations")
class GitIntegrationController(
    private val gitIntegrationService: GitIntegrationService,
) : GitIntegrationDocs {
    @PostMapping
    override fun createGitIntegration(
        @RequestBody request: CreateGitIntegrationRequest,
    ) = gitIntegrationService.createGitIntegration(request)

    @GetMapping
    override fun getGitIntegrations(): List<GitIntegrationResponse> = gitIntegrationService.getGitIntegrations()

    @GetMapping("/{integrationId}")
    override fun getGitIntegration(
        @PathVariable integrationId: Long,
    ): GitIntegrationResponse = gitIntegrationService.getGitIntegration(integrationId)

    @DeleteMapping("/{integrationId}")
    override fun deleteGitIntegration(
        @PathVariable integrationId: Long,
    ) = gitIntegrationService.deleteGitIntegration(integrationId)
}

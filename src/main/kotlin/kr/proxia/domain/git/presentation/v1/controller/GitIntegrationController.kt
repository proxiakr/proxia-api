package kr.proxia.domain.git.presentation.v1.controller

import kr.proxia.domain.git.application.service.GitIntegrationService
import kr.proxia.domain.git.presentation.v1.docs.GitIntegrationDocs
import kr.proxia.domain.git.presentation.v1.request.CreateGitIntegrationRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/git/integrations")
class GitIntegrationController(private val gitIntegrationService: GitIntegrationService) : GitIntegrationDocs {
    @PostMapping
    override fun createGitIntegration(@RequestBody request: CreateGitIntegrationRequest) = gitIntegrationService.createGitIntegration(request)
}
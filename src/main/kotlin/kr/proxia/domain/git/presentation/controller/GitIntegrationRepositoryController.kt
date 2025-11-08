package kr.proxia.domain.git.presentation.controller

import kr.proxia.domain.git.application.service.GitIntegrationRepositoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/git/integration/{gitIntegrationId}/repositories")
class GitIntegrationRepositoryController(
    private val gitIntegrationRepositoryService: GitIntegrationRepositoryService,
) {
    @GetMapping
    fun getRepositories(@PathVariable gitIntegrationId: Long) =
        gitIntegrationRepositoryService.getRepositories(gitIntegrationId)
}
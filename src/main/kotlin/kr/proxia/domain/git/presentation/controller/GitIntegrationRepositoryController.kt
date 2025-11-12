package kr.proxia.domain.git.presentation.controller

import kr.proxia.domain.git.application.service.GitIntegrationRepositoryService
import kr.proxia.domain.git.presentation.docs.GitIntegrationRepositoryDocs
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/git/integrations/{gitIntegrationId}/repositories")
class GitIntegrationRepositoryController(
    private val gitIntegrationRepositoryService: GitIntegrationRepositoryService,
) : GitIntegrationRepositoryDocs {
    @GetMapping
    override fun getRepositories(
        @PathVariable gitIntegrationId: UUID,
    ) = gitIntegrationRepositoryService.getRepositories(gitIntegrationId)
}

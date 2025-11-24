package kr.proxia.domain.git.presentation.controller

import kr.proxia.domain.git.application.service.GitRepositoryService
import kr.proxia.domain.git.presentation.docs.GitRepositoryDocs
import kr.proxia.domain.git.presentation.request.CreateGitRepositoryRequest
import kr.proxia.domain.git.presentation.response.GitRepositoryResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/git/integrations/{integrationId}/repositories")
class GitRepositoryController(
    private val gitRepositoryService: GitRepositoryService,
) : GitRepositoryDocs {
    @PostMapping
    override fun createGitRepository(
        @PathVariable integrationId: UUID,
        @RequestBody request: CreateGitRepositoryRequest,
    ) = gitRepositoryService.createGitRepository(integrationId, request)

    @GetMapping
    override fun getGitRepositories(
        @PathVariable integrationId: UUID,
    ) = gitRepositoryService.getGitRepositories(integrationId)

    @DeleteMapping("/{repositoryId}")
    override fun deleteGitRepository(
        @PathVariable integrationId: UUID,
        @PathVariable repositoryId: UUID,
    ) = gitRepositoryService.deleteGitRepository(repositoryId)
}

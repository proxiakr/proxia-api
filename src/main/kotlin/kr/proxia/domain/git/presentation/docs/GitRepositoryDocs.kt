package kr.proxia.domain.git.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.git.presentation.request.CreateGitRepositoryRequest
import kr.proxia.domain.git.presentation.response.GitRepositoryResponse
import java.util.UUID

@Tag(name = "Git Repository")
interface GitRepositoryDocs {
    @Operation(summary = "Create Git Repository")
    fun createGitRepository(
        integrationId: UUID,
        request: CreateGitRepositoryRequest,
    )

    @Operation(summary = "Get Git Repositories")
    fun getGitRepositories(integrationId: UUID): List<GitRepositoryResponse>

    @Operation(summary = "Delete Git Repository")
    fun deleteGitRepository(
        integrationId: UUID,
        repositoryId: UUID,
    )
}

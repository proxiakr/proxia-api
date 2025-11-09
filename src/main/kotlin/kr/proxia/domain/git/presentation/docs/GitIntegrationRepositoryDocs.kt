package kr.proxia.domain.git.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.git.presentation.response.GitIntegrationRepositoryResponse

@Tag(name = "Git Integration Repository")
interface GitIntegrationRepositoryDocs {
    @Operation(summary = "Get Repositories")
    fun getRepositories(gitIntegrationId: Long): List<GitIntegrationRepositoryResponse>
}

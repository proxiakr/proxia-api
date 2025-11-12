package kr.proxia.domain.git.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.git.presentation.response.GitIntegrationRepositoryResponse
import java.util.UUID

@Tag(name = "Git Integration Repository")
interface GitIntegrationRepositoryDocs {
    @Operation(summary = "Get Repositories")
    fun getRepositories(gitIntegrationId: UUID): List<GitIntegrationRepositoryResponse>
}

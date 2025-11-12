package kr.proxia.domain.git.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.git.presentation.request.CreateGitIntegrationRequest
import kr.proxia.domain.git.presentation.response.GitIntegrationResponse

@Tag(name = "Git Integration")
interface GitIntegrationDocs {
    @Operation(summary = "Create Git Integration")
    fun createGitIntegration(request: CreateGitIntegrationRequest)

    @Operation(summary = "Get All Git Integrations")
    fun getGitIntegrations(): List<GitIntegrationResponse>

    @Operation(summary = "Get Git Integration")
    fun getGitIntegration(integrationId: Long): GitIntegrationResponse

    @Operation(summary = "Delete Git Integration")
    fun deleteGitIntegration(integrationId: Long)
}

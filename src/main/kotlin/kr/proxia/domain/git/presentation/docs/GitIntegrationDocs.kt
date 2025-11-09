package kr.proxia.domain.git.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.git.presentation.request.CreateGitIntegrationRequest

@Tag(name = "Git Integration")
interface GitIntegrationDocs {
    @Operation(summary = "Create Git Integration")
    fun createGitIntegration(request: CreateGitIntegrationRequest)
}

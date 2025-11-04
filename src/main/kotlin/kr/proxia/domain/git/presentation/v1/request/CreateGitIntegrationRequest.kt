package kr.proxia.domain.git.presentation.v1.request

import kr.proxia.domain.git.domain.enums.GitIntegrationProvider

data class CreateGitIntegrationRequest(
    val provider: GitIntegrationProvider,
    val code: String,
)
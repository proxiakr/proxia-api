package kr.proxia.core.domain

import kr.proxia.core.enums.GitProvider

data class CreateGitConnection(
    val installationId: String,
    val name: String,
    val provider: GitProvider,
)

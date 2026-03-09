package kr.proxia.core.api.controller.v1.request

import kr.proxia.core.domain.CreateGitConnection
import kr.proxia.core.enums.GitProvider

data class CreateGitConnectionRequest(
    val installationId: String,
    val name: String,
    val provider: GitProvider,
) {
    fun toDomain(): CreateGitConnection =
        CreateGitConnection(
            installationId = installationId,
            name = name,
            provider = provider,
        )
}

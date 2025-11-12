package kr.proxia.domain.git.presentation.response

import kr.proxia.domain.git.domain.entity.GitIntegrationEntity
import kr.proxia.domain.git.domain.enums.GitIntegrationProvider
import java.time.LocalDateTime

data class GitIntegrationResponse(
    val id: Long,
    val provider: GitIntegrationProvider,
    val expiresAt: LocalDateTime?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(entity: GitIntegrationEntity) =
            GitIntegrationResponse(
                id = entity.id,
                provider = entity.provider,
                expiresAt = entity.expiresAt,
                createdAt = entity.createdAt,
            )
    }
}

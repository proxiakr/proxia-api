package kr.proxia.domain.git.presentation.response

import kr.proxia.domain.git.domain.entity.GitRepositoryEntity
import java.time.LocalDateTime
import java.util.UUID

data class GitRepositoryResponse(
    val id: UUID,
    val fullName: String,
    val gitIntegrationId: UUID,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun of(entity: GitRepositoryEntity) =
            GitRepositoryResponse(
                id = entity.id,
                fullName = entity.fullName,
                gitIntegrationId = entity.gitIntegrationId,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )

        fun of(entities: List<GitRepositoryEntity>) = entities.map { of(it) }
    }
}

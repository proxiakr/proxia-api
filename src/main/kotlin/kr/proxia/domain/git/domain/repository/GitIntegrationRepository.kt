package kr.proxia.domain.git.domain.repository

import kr.proxia.domain.git.domain.entity.GitIntegrationEntity
import kr.proxia.domain.git.domain.enums.GitIntegrationProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface GitIntegrationRepository : JpaRepository<GitIntegrationEntity, UUID> {
    fun findByUserIdAndProvider(
        userId: UUID,
        provider: GitIntegrationProvider,
    ): GitIntegrationEntity?

    fun existsByUserIdAndProvider(
        userId: UUID,
        provider: GitIntegrationProvider,
    ): Boolean

    fun findAllByUserIdAndDeletedAtIsNull(userId: UUID): List<GitIntegrationEntity>
}

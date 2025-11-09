package kr.proxia.domain.git.domain.repository

import kr.proxia.domain.git.domain.entity.GitIntegrationEntity
import kr.proxia.domain.git.domain.enums.GitIntegrationProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GitIntegrationRepository : JpaRepository<GitIntegrationEntity, Long> {
    fun existsByUserIdAndProvider(
        userId: Long,
        provider: GitIntegrationProvider,
    ): Boolean

    fun findByUserIdAndProvider(
        userId: Long,
        provider: GitIntegrationProvider,
    ): GitIntegrationEntity?
}

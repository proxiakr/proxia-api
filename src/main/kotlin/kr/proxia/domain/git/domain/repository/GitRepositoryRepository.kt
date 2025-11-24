package kr.proxia.domain.git.domain.repository

import kr.proxia.domain.git.domain.entity.GitRepositoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface GitRepositoryRepository : JpaRepository<GitRepositoryEntity, UUID> {
    fun findByFullNameAndDeletedAtIsNull(fullName: String): GitRepositoryEntity?
    fun findAllByGitIntegrationIdAndDeletedAtIsNull(gitIntegrationId: UUID): List<GitRepositoryEntity>
}

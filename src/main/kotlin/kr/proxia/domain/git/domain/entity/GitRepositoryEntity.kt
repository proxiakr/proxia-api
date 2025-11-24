package kr.proxia.domain.git.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import kr.proxia.global.jpa.common.BaseEntity
import java.util.UUID

@Entity
@Table(
    name = "git_repositories",
    indexes = [
        Index(name = "idx_git_repos_integration", columnList = "gitIntegrationId, deletedAt"),
        Index(name = "idx_git_repos_fullname", columnList = "fullName, deletedAt")
    ]
)
class GitRepositoryEntity(
    val gitIntegrationId: UUID,
    val fullName: String  // "username/repo"
) : BaseEntity()

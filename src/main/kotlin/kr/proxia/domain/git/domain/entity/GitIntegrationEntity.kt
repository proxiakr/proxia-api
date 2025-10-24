package kr.proxia.domain.git.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import kr.proxia.domain.git.domain.enums.GitIntegrationProvider
import kr.proxia.global.jpa.common.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "git_integrations")
class GitIntegrationEntity(
    val userId: Long,
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    val provider: GitIntegrationProvider,
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresAt: LocalDateTime?
): BaseEntity()
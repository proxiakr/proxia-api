package kr.proxia.domain.deployment.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import kr.proxia.domain.deployment.domain.enums.DeploymentStatus
import kr.proxia.global.jpa.common.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "deployments")
class DeploymentEntity(
    val projectId: Long,
    commitSha: String,
    commitMessage: String?,
    commitAuthor: String?,
    val branch: String,
    status: DeploymentStatus = DeploymentStatus.PENDING,
    val startedAt: LocalDateTime?,
    val finishedAt: LocalDateTime?,
) : BaseEntity() {
    var commitSha: String = commitSha
        protected set

    var commitMessage: String? = commitMessage
        protected set

    var commitAuthor: String? = commitAuthor
        protected set

    @Enumerated(EnumType.STRING)
    var status: DeploymentStatus = status
        protected set

    fun update(
        commitSha: String = this.commitSha,
        commitMessage: String? = this.commitMessage,
        commitAuthor: String? = this.commitAuthor,
        status: DeploymentStatus = this.status,
    ) {
        this.commitSha = commitSha
        this.commitMessage = commitMessage
        this.commitAuthor = commitAuthor
        this.status = status
    }
}

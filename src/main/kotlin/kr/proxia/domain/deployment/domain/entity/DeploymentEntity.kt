package kr.proxia.domain.deployment.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.Table
import kr.proxia.domain.deployment.domain.enums.DeploymentStatus
import kr.proxia.global.jpa.common.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "deployments")
class DeploymentEntity(
    val projectId: Long,
    val commitSha: String,
    val commitMessage: String?,
    val commitAuthor: String?,
    val branch: String,
    status: DeploymentStatus = DeploymentStatus.PENDING,
    val startedAt: LocalDateTime?,
    val finishedAt: LocalDateTime?,
) : BaseEntity() {
    var status: DeploymentStatus = status
        protected set

    fun update(status: DeploymentStatus = this.status) {
        this.status = status
    }
}

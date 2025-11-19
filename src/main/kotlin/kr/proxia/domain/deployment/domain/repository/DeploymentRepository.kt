package kr.proxia.domain.deployment.domain.repository

import kr.proxia.domain.deployment.domain.entity.DeploymentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DeploymentRepository : JpaRepository<DeploymentEntity, UUID> {
    fun findAllByProjectIdAndDeletedAtIsNull(projectId: UUID): List<DeploymentEntity>

    fun findAllByProjectIdAndDeletedAtIsNullOrderByCreatedAtDesc(projectId: UUID): List<DeploymentEntity>
}

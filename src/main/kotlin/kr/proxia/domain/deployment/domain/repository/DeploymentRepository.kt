package kr.proxia.domain.deployment.domain.repository

import kr.proxia.domain.deployment.domain.entity.DeploymentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DeploymentRepository : JpaRepository<DeploymentEntity, Long> {
    fun findAllByProjectIdOrderByCreatedAtDesc(projectId: Long): List<DeploymentEntity>
}

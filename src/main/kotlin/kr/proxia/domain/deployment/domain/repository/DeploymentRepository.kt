package kr.proxia.domain.deployment.domain.repository

import kr.proxia.domain.deployment.domain.entity.DeploymentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DeploymentRepository : JpaRepository<DeploymentEntity, UUID> {
    fun findAllByServiceIdAndDeletedAtIsNull(serviceId: UUID): List<DeploymentEntity>

    fun findAllByServiceIdAndDeletedAtIsNullOrderByCreatedAtDesc(serviceId: UUID): List<DeploymentEntity>

    fun existsByServiceId(serviceId: UUID): Boolean
}

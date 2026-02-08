package kr.proxia.storage.db.core.repository

import kr.proxia.storage.db.core.entity.Deployment
import kr.proxia.storage.db.core.entity.DeploymentLog
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DeploymentLogRepository : JpaRepository<DeploymentLog, UUID> {
    fun findAllByDeploymentOrderByCreatedAt(deployment: Deployment): List<DeploymentLog>
}

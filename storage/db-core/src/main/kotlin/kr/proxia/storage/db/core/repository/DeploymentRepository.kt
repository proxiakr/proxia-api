package kr.proxia.storage.db.core.repository

import kr.proxia.storage.db.core.entity.AppService
import kr.proxia.storage.db.core.entity.Deployment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DeploymentRepository : JpaRepository<Deployment, UUID> {
    fun findAllByAppServiceOrderByCreatedAtDesc(appService: AppService): List<Deployment>
}

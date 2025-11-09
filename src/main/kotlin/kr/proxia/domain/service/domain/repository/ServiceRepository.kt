package kr.proxia.domain.service.domain.repository

import kr.proxia.domain.service.domain.entity.ServiceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceRepository : JpaRepository<ServiceEntity, Long> {
    fun findAllByProjectId(projectId: Long): List<ServiceEntity>

    fun existsByProjectIdAndName(
        projectId: Long,
        name: String,
    ): Boolean
}

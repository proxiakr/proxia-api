package kr.proxia.domain.service.domain.repository

import kr.proxia.domain.service.domain.entity.ServiceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ServiceRepository : JpaRepository<ServiceEntity, UUID> {
    fun findAllByProjectIdAndDeletedAtIsNull(projectId: UUID): List<ServiceEntity>

    fun findAllByProjectIdAndDeletedAtIsNullAndTargetIdIsNotNull(projectId: UUID): List<ServiceEntity>

    fun findByIdAndDeletedAtIsNull(id: UUID): ServiceEntity?

    fun findAllByTargetIdIsNotNullAndDeletedAtIsNull(): List<ServiceEntity>
}

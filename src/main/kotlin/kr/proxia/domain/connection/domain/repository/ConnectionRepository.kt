package kr.proxia.domain.connection.domain.repository

import kr.proxia.domain.connection.domain.entity.ConnectionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ConnectionRepository : JpaRepository<ConnectionEntity, UUID> {
    fun findAllByProjectIdAndDeletedAtIsNull(projectId: UUID): List<ConnectionEntity>

    fun existsBySourceIdAndTargetIdAndDeletedAtIsNull(
        sourceId: UUID,
        targetId: UUID,
    ): Boolean

    fun findAllBySourceIdOrTargetIdAndDeletedAtIsNull(
        sourceId: UUID,
        targetId: UUID,
    ): List<ConnectionEntity>

    fun findByIdAndDeletedAtIsNull(id: UUID): ConnectionEntity?
}

package kr.proxia.domain.connection.domain.repository

import kr.proxia.domain.connection.domain.entity.ConnectionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ConnectionRepository : JpaRepository<ConnectionEntity, Long> {
    fun findAllByProjectIdAndDeletedAtIsNull(projectId: Long): List<ConnectionEntity>

    fun existsBySourceIdAndTargetIdAndDeletedAtIsNull(
        sourceId: Long,
        targetId: Long,
    ): Boolean

    fun findAllBySourceIdOrTargetIdAndDeletedAtIsNull(
        sourceId: Long,
        targetId: Long,
    ): List<ConnectionEntity>
}

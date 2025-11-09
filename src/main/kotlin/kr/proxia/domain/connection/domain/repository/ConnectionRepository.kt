package kr.proxia.domain.connection.domain.repository

import kr.proxia.domain.connection.domain.entity.ConnectionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConnectionRepository : JpaRepository<ConnectionEntity, Long> {
    fun findAllByProjectId(projectId: Long): List<ConnectionEntity>

    fun existsBySourceIdAndTargetId(
        sourceId: Long,
        targetId: Long,
    ): Boolean

    fun findAllBySourceIdOrTargetId(
        sourceId: Long,
        targetId: Long,
    ): List<ConnectionEntity>
}

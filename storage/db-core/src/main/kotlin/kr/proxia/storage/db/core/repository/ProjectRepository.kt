package kr.proxia.storage.db.core.repository

import kr.proxia.storage.db.core.entity.Project
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ProjectRepository : JpaRepository<Project, UUID> {
    @Query(
        value = "SELECT * FROM projects WHERE (:cursor IS NULL OR id < :cursor) ORDER BY id DESC LIMIT :limit",
        nativeQuery = true,
    )
    fun findWithCursor(
        cursor: UUID?,
        limit: Int,
    ): List<Project>
}

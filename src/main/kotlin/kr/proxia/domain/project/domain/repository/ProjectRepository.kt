package kr.proxia.domain.project.domain.repository

import kr.proxia.domain.project.domain.entity.ProjectEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProjectRepository : JpaRepository<ProjectEntity, UUID> {
    fun findAllByUserIdAndDeletedAtIsNull(
        userId: UUID,
        pageable: Pageable,
    ): Page<ProjectEntity>

    fun existsBySlugAndDeletedAtIsNull(slug: String): Boolean

    fun findByIdAndDeletedAtIsNull(id: UUID): ProjectEntity?
}

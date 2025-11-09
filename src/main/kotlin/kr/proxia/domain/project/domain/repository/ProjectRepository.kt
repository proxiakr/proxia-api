package kr.proxia.domain.project.domain.repository

import kr.proxia.domain.project.domain.entity.ProjectEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository : JpaRepository<ProjectEntity, Long> {
    fun findAllByUserId(
        userId: Long,
        pageable: Pageable,
    ): Page<ProjectEntity>
}

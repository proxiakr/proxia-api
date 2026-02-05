package kr.proxia.storage.db.core.repository

import kr.proxia.storage.db.core.entity.Project
import kr.proxia.storage.db.core.entity.Workspace
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProjectRepository : JpaRepository<Project, UUID> {
    fun findByIdAndWorkspace(
        id: UUID,
        workspace: Workspace,
    ): Project?

    fun findAllByWorkspace(workspace: Workspace): List<Project>
}

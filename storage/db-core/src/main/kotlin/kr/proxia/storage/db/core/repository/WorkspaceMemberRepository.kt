package kr.proxia.storage.db.core.repository

import kr.proxia.storage.db.core.entity.Workspace
import kr.proxia.storage.db.core.entity.WorkspaceMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface WorkspaceMemberRepository : JpaRepository<WorkspaceMember, UUID> {
    fun findByWorkspace(workspace: Workspace): List<WorkspaceMember>

    @Query("SELECT m FROM WorkspaceMember m JOIN FETCH m.user WHERE m.workspace = :workspace")
    fun findByWorkspaceWithUser(workspace: Workspace): List<WorkspaceMember>
}

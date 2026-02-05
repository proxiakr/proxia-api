package kr.proxia.storage.db.core.repository

import kr.proxia.storage.db.core.entity.Workspace
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface WorkspaceRepository : JpaRepository<Workspace, UUID> {
    @Query(
        """
        SELECT w FROM Workspace w
        JOIN WorkspaceMember wm ON w.id = wm.workspace.id
        WHERE wm.user.id = :userId
        ORDER BY w.id DESC
    """,
    )
    fun findByMember(userId: UUID): List<Workspace>

    @Query(
        """
        SELECT w
        FROM Workspace w
        WHERE w.id = :workspaceId
        AND EXISTS (
            SELECT 1
            FROM WorkspaceMember wm
            WHERE wm.workspace = w
              AND wm.user.id = :userId
        )
    """,
    )
    fun findByIdAndMember(
        workspaceId: UUID,
        userId: UUID,
    ): Workspace?
}

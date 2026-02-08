package kr.proxia.storage.db.core.repository

import kr.proxia.storage.db.core.entity.GitConnection
import kr.proxia.storage.db.core.entity.Workspace
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GitConnectionRepository : JpaRepository<GitConnection, UUID> {
    fun findAllByWorkspace(workspace: Workspace): List<GitConnection>
}

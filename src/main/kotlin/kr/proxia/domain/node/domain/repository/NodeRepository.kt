package kr.proxia.domain.node.domain.repository

import kr.proxia.domain.node.domain.entity.NodeEntity
import kr.proxia.domain.node.domain.enums.NodeStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface NodeRepository : JpaRepository<NodeEntity, UUID> {
    fun findByStatus(status: NodeStatus): List<NodeEntity>
}

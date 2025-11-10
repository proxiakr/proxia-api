package kr.proxia.domain.connection.presentation.response

import kr.proxia.domain.connection.domain.entity.ConnectionEntity
import kr.proxia.domain.connection.domain.enums.ConnectionType
import java.time.LocalDateTime

data class ConnectionResponse(
    val id: Long,
    val projectId: Long,
    val sourceId: Long,
    val targetId: Long,
    val type: ConnectionType,
    val label: String?,
    val weight: Int?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(connection: ConnectionEntity) =
            ConnectionResponse(
                id = connection.id,
                projectId = connection.projectId,
                sourceId = connection.sourceId,
                targetId = connection.targetId,
                type = connection.type,
                label = connection.label,
                weight = connection.weight,
                createdAt = connection.createdAt,
                updatedAt = connection.updatedAt,
            )
    }
}

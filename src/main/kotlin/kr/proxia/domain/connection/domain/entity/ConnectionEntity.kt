package kr.proxia.domain.connection.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import kr.proxia.domain.connection.domain.enums.ConnectionType
import kr.proxia.global.jpa.common.BaseEntity
import java.util.UUID

@Entity
@Table(
    name = "connections",
    indexes = [
        Index(name = "idx_connections_project_deleted", columnList = "projectId, deletedAt"),
        Index(name = "idx_connections_source_deleted", columnList = "sourceId, deletedAt"),
        Index(name = "idx_connections_target_deleted", columnList = "targetId, deletedAt"),
        Index(name = "idx_connections_source_target_deleted", columnList = "sourceId, targetId, deletedAt"),
    ],
)
class ConnectionEntity(
    val projectId: UUID,
    val sourceId: UUID,
    val targetId: UUID,
    type: ConnectionType,
    label: String?,
    weight: Int?,
) : BaseEntity() {
    @Enumerated(EnumType.STRING)
    var type: ConnectionType = type
        protected set

    var label: String? = label
        protected set

    var weight: Int? = weight
        protected set

    fun update(
        type: ConnectionType = this.type,
        label: String? = this.label,
        weight: Int? = this.weight,
    ) {
        this.type = type
        this.label = label
        this.weight = weight
    }
}

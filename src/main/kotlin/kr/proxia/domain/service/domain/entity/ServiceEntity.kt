package kr.proxia.domain.service.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import kr.proxia.domain.service.domain.enums.ServiceType
import kr.proxia.global.jpa.common.BaseEntity
import java.util.UUID

@Entity
@Table(
    name = "services",
    indexes = [
        Index(name = "idx_services_project_deleted", columnList = "projectId, deletedAt"),
        Index(name = "idx_services_user_deleted", columnList = "userId, deletedAt"),
    ],
)
class ServiceEntity(
    val projectId: UUID,
    val userId: UUID,
    name: String,
    description: String?,
    type: ServiceType,
    x: Double,
    y: Double,
    targetId: UUID?,
) : BaseEntity() {
    var name: String = name
        protected set

    var description: String? = description
        protected set

    @Enumerated(EnumType.STRING)
    var type: ServiceType = type
        protected set

    var x: Double = x
        protected set

    var y: Double = y
        protected set

    var targetId: UUID? = targetId
        protected set

    fun update(
        name: String = this.name,
        description: String? = this.description,
        type: ServiceType = this.type,
        targetId: UUID? = this.targetId,
    ) {
        this.name = name
        this.description = description
        this.type = type
        this.targetId = targetId
    }

    fun updatePosition(
        x: Double = this.x,
        y: Double = this.y,
    ) {
        this.x = x
        this.y = y
    }
}

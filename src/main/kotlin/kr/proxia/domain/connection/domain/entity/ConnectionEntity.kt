package kr.proxia.domain.connection.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import kr.proxia.domain.connection.domain.enums.ConnectionType
import kr.proxia.global.jpa.common.BaseEntity

@Entity
@Table(name = "connections")
class ConnectionEntity(
    val projectId: Long,
    val sourceId: Long,
    val targetId: Long,
    type: ConnectionType,
    label: String?,
) : BaseEntity() {
    @Enumerated(EnumType.STRING)
    var type: ConnectionType = type
        protected set

    var label: String? = label
        protected set

    fun update(
        type: ConnectionType = this.type,
        label: String? = this.label,
    ) {
        this.type = type
        this.label = label
    }
}

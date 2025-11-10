package kr.proxia.domain.service.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import kr.proxia.domain.service.domain.enums.ServiceType
import kr.proxia.global.jpa.common.BaseEntity

@Entity
@Table(name = "services")
class ServiceEntity(
    val projectId: Long,
    val userId: Long,
    name: String,
    description: String?,
    type: ServiceType,
    x: Double,
    y: Double,
    width: Double,
    height: Double,
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

    var width: Double = width
        protected set

    var height: Double = height
        protected set

    fun update(
        name: String = this.name,
        description: String? = this.description,
        type: ServiceType = this.type,
    ) {
        this.name = name
        this.description = description
        this.type = type
    }

    fun updatePosition(
        x: Double = this.x,
        y: Double = this.y,
        width: Double = this.width,
        height: Double = this.height,
    ) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }
}

package kr.proxia.domain.container.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import kr.proxia.domain.container.domain.enums.ContainerStatus
import kr.proxia.global.jpa.common.BaseEntity

@Entity
@Table(
    name = "containers",
    indexes = [
        Index(name = "idx_containers_service_deleted", columnList = "serviceId, deletedAt"),
        Index(name = "idx_containers_container_id", columnList = "containerId"),
    ],
)
class ContainerEntity(
    val serviceId: Long,
    containerId: String?,
    imageId: String?,
    imageName: String?,
    status: ContainerStatus,
    port: Int?,
    internalPort: Int,
) : BaseEntity() {
    var containerId: String? = containerId
        protected set

    var imageId: String? = imageId
        protected set

    var imageName: String? = imageName
        protected set

    @Enumerated(EnumType.STRING)
    var status: ContainerStatus = status
        protected set

    var port: Int? = port
        protected set

    var internalPort: Int = internalPort
        protected set

    fun update(
        containerId: String? = this.containerId,
        imageId: String? = this.imageId,
        imageName: String? = this.imageName,
        status: ContainerStatus = this.status,
        port: Int? = this.port,
    ) {
        this.containerId = containerId
        this.imageId = imageId
        this.imageName = imageName
        this.status = status
        this.port = port
    }

    fun updateStatus(status: ContainerStatus) {
        this.status = status
    }
}

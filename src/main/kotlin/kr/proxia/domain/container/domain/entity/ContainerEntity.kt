package kr.proxia.domain.container.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.Table
import kr.proxia.domain.container.domain.enums.ContainerStatus
import kr.proxia.global.jpa.common.BaseEntity
import java.util.UUID

@Entity
@Table(name = "containers")
class ContainerEntity(
    val serviceId: UUID,
    val nodeId: UUID,
    val containerId: String,
    val imageId: String,
    val imageName: String,
    val status: ContainerStatus = ContainerStatus.RUNNING,
    val port: Int?,
    val internalPort: Int,
) : BaseEntity()

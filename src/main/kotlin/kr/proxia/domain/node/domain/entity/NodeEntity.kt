package kr.proxia.domain.node.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import kr.proxia.domain.node.domain.enums.NodeStatus
import kr.proxia.global.container.enums.ContainerRuntimeType
import kr.proxia.global.jpa.common.BaseEntity

@Entity
@Table(name = "nodes")
class NodeEntity(
    val name: String,
    val endpoint: String,
    runtimeType: ContainerRuntimeType,
    status: NodeStatus = NodeStatus.ACTIVE,
    containerCount: Int = 0,
) : BaseEntity() {
    @Enumerated(EnumType.STRING)
    var runtimeType: ContainerRuntimeType = runtimeType
        protected set

    @Enumerated(EnumType.STRING)
    var status: NodeStatus = status
        protected set

    var containerCount: Int = containerCount
        protected set

    fun update(
        runtimeType: ContainerRuntimeType = this.runtimeType,
        status: NodeStatus = this.status,
        containerCount: Int = this.containerCount,
    ) {
        this.runtimeType = runtimeType
        this.status = status
        this.containerCount = containerCount
    }
}

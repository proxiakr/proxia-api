package kr.proxia.domain.container.domain.repository

import kr.proxia.domain.container.domain.entity.ContainerEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ContainerRepository : JpaRepository<ContainerEntity, Long> {
    fun findByServiceIdAndDeletedAtIsNull(serviceId: Long): ContainerEntity?

    fun findByContainerIdAndDeletedAtIsNull(containerId: String): ContainerEntity?
}

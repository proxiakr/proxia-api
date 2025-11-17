package kr.proxia.domain.container.domain.repository

import kr.proxia.domain.container.domain.entity.ContainerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ContainerRepository : JpaRepository<ContainerEntity, UUID> {
}

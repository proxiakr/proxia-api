package kr.proxia.domain.resource.domain.repository

import kr.proxia.domain.resource.domain.entity.DatabaseResourceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DatabaseResourceRepository : JpaRepository<DatabaseResourceEntity, UUID>

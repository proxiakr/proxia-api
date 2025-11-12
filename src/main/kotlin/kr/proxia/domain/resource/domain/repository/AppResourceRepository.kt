package kr.proxia.domain.resource.domain.repository

import kr.proxia.domain.resource.domain.entity.AppResourceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AppResourceRepository : JpaRepository<AppResourceEntity, UUID>

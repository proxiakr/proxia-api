package kr.proxia.storage.db.core.repository

import kr.proxia.storage.db.core.entity.Service
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ServiceRepository : JpaRepository<Service, UUID>

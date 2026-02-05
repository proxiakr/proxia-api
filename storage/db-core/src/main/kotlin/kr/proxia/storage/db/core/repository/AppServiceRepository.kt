package kr.proxia.storage.db.core.repository

import kr.proxia.storage.db.core.entity.AppService
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AppServiceRepository : JpaRepository<AppService, UUID>

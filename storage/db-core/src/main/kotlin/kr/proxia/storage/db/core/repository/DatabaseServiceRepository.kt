package kr.proxia.storage.db.core.repository

import kr.proxia.storage.db.core.entity.DatabaseService
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DatabaseServiceRepository : JpaRepository<DatabaseService, UUID>

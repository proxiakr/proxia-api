package kr.proxia.storage.db.core.repository

import kr.proxia.core.enums.AuthProvider
import kr.proxia.storage.db.core.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByProviderAndProviderId(provider: AuthProvider, providerId: String): User?
}

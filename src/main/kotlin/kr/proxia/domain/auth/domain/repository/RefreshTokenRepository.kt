package kr.proxia.domain.auth.domain.repository

import kr.proxia.domain.auth.domain.entity.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByUserIdAndRefreshToken(
        userId: UUID,
        refreshToken: String,
    ): RefreshTokenEntity?

    fun deleteByUserId(userId: UUID)
}

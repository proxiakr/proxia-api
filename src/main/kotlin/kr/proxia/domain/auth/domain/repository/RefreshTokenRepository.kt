package kr.proxia.domain.auth.domain.repository

import kr.proxia.domain.auth.domain.entity.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {
    fun findByUserIdAndRefreshToken(
        userId: Long,
        refreshToken: String,
    ): RefreshTokenEntity?

    fun deleteByUserId(userId: Long)
}

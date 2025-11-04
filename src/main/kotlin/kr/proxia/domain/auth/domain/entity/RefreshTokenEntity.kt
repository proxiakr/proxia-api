package kr.proxia.domain.auth.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.Table
import kr.proxia.global.jpa.common.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    val userId: Long,
    refreshToken: String,
    val expiresAt: LocalDateTime,
) : BaseEntity() {
    var refreshToken = refreshToken
        protected set

    fun update(refreshToken: String = this.refreshToken) {
        this.refreshToken = refreshToken
    }
}
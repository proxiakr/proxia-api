package kr.proxia.domain.auth.domain.repository

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RefreshTokenRepository(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }

    fun save(userId: Long, refreshToken: String) {
        redisTemplate.opsForValue().set("$REFRESH_TOKEN_KEY:$userId", refreshToken, Duration.ofDays(7))
    }

    fun findByUserId(userId: Long): String? {
        return redisTemplate.opsForValue().get("$REFRESH_TOKEN_KEY:$userId")
    }

    fun deleteByUserId(userId: Long) {
        redisTemplate.delete("$REFRESH_TOKEN_KEY:$userId")
    }
}
package kr.proxia.global.security.jwt.provider

import io.jsonwebtoken.Jwts
import kr.proxia.domain.auth.domain.entity.RefreshTokenEntity
import kr.proxia.domain.auth.domain.repository.RefreshTokenRepository
import kr.proxia.domain.user.domain.entity.UserEntity
import kr.proxia.domain.user.domain.enums.UserRole
import kr.proxia.domain.user.domain.repository.UserRepository
import kr.proxia.global.security.jwt.enums.JwtType
import kr.proxia.global.security.jwt.properties.JwtProperties
import org.springframework.stereotype.Component

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {
    private fun getClaims(token: String) = Jwts.parser()
        .verifyWith(jwtProperties.secretKeySpec)
        .build()
        .parseSignedClaims(token)

    fun getSubject(token: String): Long = getClaims(token).payload.subject.toLong()

    fun getRole(token: String): UserRole = UserRole.valueOf(
            getClaims(token)
                .payload
            .get("role", String::class.java)
    )

    fun getType(token: String): JwtType = JwtType.valueOf(
        getClaims(token)
            .header
            .type
    )

    fun createAccessToken(userId: Long, role: UserRole): String {
        return Jwts.builder()
            .header()
            .type(JwtType.ACCESS.name)
            .and()
            .subject(userId.toString())
            .claim("role", role.name)
            .signWith(jwtProperties.secretKeySpec)
            .compact()
    }

    fun createRefreshToken(userId: Long): String {
        return Jwts.builder()
            .header()
            .type(JwtType.REFRESH.name)
            .and()
            .subject(userId.toString())
            .signWith(jwtProperties.secretKeySpec)
            .compact()
    }
}
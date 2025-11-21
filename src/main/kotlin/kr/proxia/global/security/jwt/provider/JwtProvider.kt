package kr.proxia.global.security.jwt.provider

import io.jsonwebtoken.Jwts
import kr.proxia.domain.user.domain.enums.UserRole
import kr.proxia.global.security.jwt.enums.JwtType
import kr.proxia.global.security.jwt.properties.JwtProperties
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {
    fun createAccessToken(
        userId: UUID,
        role: UserRole,
    ): String =
        Jwts
            .builder()
            .subject(userId.toString())
            .claim(ROLE_KEY, role.name.lowercase())
            .claim(TYPE_KEY, JwtType.ACCESS.name.lowercase())
            .signWith(jwtProperties.secretKeySpec)
            .compact()

    fun createRefreshToken(userId: UUID): String =
        Jwts
            .builder()
            .subject(userId.toString())
            .claim(TYPE_KEY, JwtType.REFRESH.name.lowercase())
            .signWith(jwtProperties.secretKeySpec)
            .compact()

    companion object {
        private const val ROLE_KEY = "role"
        private const val TYPE_KEY = "type"
    }
}

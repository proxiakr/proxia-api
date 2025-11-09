package kr.proxia.global.security.jwt.provider

import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import kr.proxia.domain.user.domain.enums.UserRole
import kr.proxia.global.security.jwt.enums.JwtType
import kr.proxia.global.security.jwt.properties.JwtProperties
import org.springframework.stereotype.Component

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {
    fun createAccessToken(
        userId: Long,
        role: UserRole,
    ): String =
        Jwts
            .builder()
            .subject(userId.toString())
            .lowerClaim(ROLE_KEY, role.name)
            .lowerClaim(TYPE_KEY, JwtType.ACCESS.name)
            .signWith(jwtProperties.secretKeySpec)
            .compact()

    fun createRefreshToken(userId: Long): String =
        Jwts
            .builder()
            .subject(userId.toString())
            .lowerClaim(TYPE_KEY, JwtType.REFRESH.name)
            .signWith(jwtProperties.secretKeySpec)
            .compact()

    private fun JwtBuilder.lowerClaim(
        name: String,
        value: String,
    ) = this.claim(name, value.lowercase())

    companion object {
        private const val ROLE_KEY = "role"
        private const val TYPE_KEY = "type"
    }
}

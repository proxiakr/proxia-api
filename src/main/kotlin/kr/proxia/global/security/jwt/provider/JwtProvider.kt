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
    fun getSubject(token: String): Long = getPayload(token).subject.toLong()

    fun getRole(token: String): UserRole = UserRole.valueOf(getPayload(token).get("role", String::class.java))

    fun getType(token: String): JwtType = JwtType.valueOf(getPayload(token).get("type", String::class.java))

    fun createAccessToken(userId: Long, role: UserRole): String {
        return Jwts.builder()
            .subject(userId.toString())
            .lowerClaim("role", role.name)
            .lowerClaim("type", JwtType.ACCESS.name)
            .signWith(jwtProperties.secretKeySpec)
            .compact()
    }

    fun createRefreshToken(userId: Long): String {
        return Jwts.builder()
            .subject(userId.toString())
            .lowerClaim("type", JwtType.REFRESH.name)
            .signWith(jwtProperties.secretKeySpec)
            .compact()
    }

    private fun getPayload(token: String) = Jwts.parser()
        .verifyWith(jwtProperties.secretKeySpec)
        .build()
        .parseSignedClaims(token)
        .payload

    private fun JwtBuilder.lowerClaim(name: String, value: String) = this.claim(name, value.lowercase())
}
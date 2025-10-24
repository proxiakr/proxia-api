package kr.proxia.global.security.jwt.provider

import io.jsonwebtoken.Claims
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

    fun getRole(token: String): UserRole = UserRole.valueOf(getPayload(token).getUpper(ROLE_KEY))

    fun getType(token: String): JwtType = JwtType.valueOf(getPayload(token).getUpper(TYPE_KEY))

    fun createAccessToken(userId: Long, role: UserRole): String {
        return Jwts.builder()
            .subject(userId.toString())
            .lowerClaim(ROLE_KEY, role.name)
            .lowerClaim(TYPE_KEY, JwtType.ACCESS.name)
            .signWith(jwtProperties.secretKeySpec)
            .compact()
    }

    fun createRefreshToken(userId: Long): String {
        return Jwts.builder()
            .subject(userId.toString())
            .lowerClaim(TYPE_KEY, JwtType.REFRESH.name)
            .signWith(jwtProperties.secretKeySpec)
            .compact()
    }

    private fun getPayload(token: String) = Jwts.parser()
        .verifyWith(jwtProperties.secretKeySpec)
        .build()
        .parseSignedClaims(token)
        .payload

    private fun JwtBuilder.lowerClaim(name: String, value: String) = this.claim(name, value.lowercase())

    private fun Claims.getUpper(name: String) = this.get(name, String::class.java).uppercase()

    companion object {
        private const val ROLE_KEY = "role"
        private const val TYPE_KEY = "type"
    }
}
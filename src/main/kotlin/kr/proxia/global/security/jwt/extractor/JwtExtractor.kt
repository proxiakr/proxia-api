package kr.proxia.global.security.jwt.extractor

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import kr.proxia.domain.user.domain.enums.UserRole
import kr.proxia.global.security.jwt.enums.JwtType
import kr.proxia.global.security.jwt.properties.JwtProperties
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.text.uppercase

@Component
class JwtExtractor(
    private val jwtProperties: JwtProperties,
) {
    fun getSubject(token: String): UUID = UUID.fromString(getPayload(token).subject)

    fun getRole(token: String): UserRole = UserRole.valueOf(getPayload(token).getUpper(ROLE_KEY))

    fun getType(token: String): JwtType = JwtType.valueOf(getPayload(token).getUpper(TYPE_KEY))

    private fun getPayload(token: String) =
        Jwts
            .parser()
            .verifyWith(jwtProperties.secretKeySpec)
            .build()
            .parseSignedClaims(token)
            .payload

    private fun Claims.getUpper(name: String) = this.get(name, String::class.java).uppercase()

    companion object {
        private const val ROLE_KEY = "role"
        private const val TYPE_KEY = "type"
    }
}

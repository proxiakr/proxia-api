package kr.proxia.global.security.jwt.validator

import io.jsonwebtoken.Jwts
import kr.proxia.global.security.jwt.properties.JwtProperties
import org.springframework.stereotype.Component

@Component
class JwtValidator(private val jwtProperties: JwtProperties) {
    fun validateToken(token: String): Boolean = try {
        Jwts.parser()
            .verifyWith(jwtProperties.secretKeySpec)
            .build()
            .parseSignedClaims(token)
        true
    } catch (_: Exception) {
        false
    }
}
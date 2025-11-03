package kr.proxia.global.security.jwt.validator

import io.jsonwebtoken.Jwts
import kr.proxia.global.security.jwt.enums.JwtType
import kr.proxia.global.security.jwt.extractor.JwtExtractor
import kr.proxia.global.security.jwt.properties.JwtProperties
import org.springframework.stereotype.Component

@Component
class JwtValidator(
    private val jwtProperties: JwtProperties,
    private val jwtExtractor: JwtExtractor,
) {
    fun validateToken(token: String) = try {
        Jwts.parser()
            .verifyWith(jwtProperties.secretKeySpec)
            .build()
            .parseSignedClaims(token)

        Unit
    } catch (_: Exception) {
        throw IllegalArgumentException("Invalid token")
    }

    fun validateRefreshToken(refreshToken: String) {
        validateToken(refreshToken)

        if (jwtExtractor.getType(refreshToken) != JwtType.REFRESH)
            throw IllegalArgumentException("Invalid token type")
    }
}
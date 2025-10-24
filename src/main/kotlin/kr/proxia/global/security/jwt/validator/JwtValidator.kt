package kr.proxia.global.security.jwt.validator

import io.jsonwebtoken.Jwts
import kr.proxia.global.security.jwt.enums.JwtType
import kr.proxia.global.security.jwt.properties.JwtProperties
import kr.proxia.global.security.jwt.provider.JwtProvider
import org.springframework.stereotype.Component

@Component
class JwtValidator(
    private val jwtProperties: JwtProperties,
    private val jwtProvider: JwtProvider,
) {
    fun validateToken(token: String) = try {
        Jwts.parser()
            .verifyWith(jwtProperties.secretKeySpec)
            .build()
            .parseSignedClaims(token)
    } catch (_: Exception) {
        throw IllegalArgumentException("Invalid token")
    }

    fun validateRefreshToken(refreshToken: String) {
        validateToken(refreshToken)

        if (jwtProvider.getType(refreshToken) != JwtType.REFRESH)
            throw IllegalArgumentException("Invalid token type")
    }
}
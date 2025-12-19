package kr.proxia.global.security.jwt.validator

import io.jsonwebtoken.Jwts
import kr.proxia.domain.auth.domain.error.AuthError
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.jwt.enums.JwtType
import kr.proxia.global.security.jwt.extractor.JwtExtractor
import kr.proxia.global.security.jwt.properties.JwtProperties
import org.springframework.stereotype.Component

@Component
class JwtValidator(
    private val jwtProperties: JwtProperties,
    private val jwtExtractor: JwtExtractor,
) {
    fun validateToken(token: String) =
        try {
            Jwts
                .parser()
                .verifyWith(jwtProperties.secretKeySpec)
                .build()
                .parseSignedClaims(token)

            Unit
        } catch (_: Exception) {
            throw BusinessException(AuthError.InvalidToken)
        }

    fun validateRefreshToken(refreshToken: String) {
        validateToken(refreshToken)

        if (jwtExtractor.getType(refreshToken) != JwtType.REFRESH) {
            throw BusinessException(AuthError.InvalidToken)
        }
    }
}
